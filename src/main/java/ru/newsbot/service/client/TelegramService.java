package ru.newsbot.service.client;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.InputMediaPhoto;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMediaGroup;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.MessagesResponse;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import ru.newsbot.config.factory.ChatAdminsFactory;
import ru.newsbot.config.properties.ChatAdmin;
import ru.newsbot.model.ModerateMessage;
import ru.newsbot.model.Post;
import ru.newsbot.model.SendMessage;
import ru.newsbot.model.SendPhoto;
import ru.newsbot.model.enums.ModerationStatusEnum;
import ru.newsbot.model.enums.PostStatusEnum;
import ru.newsbot.service.MessageFormatter;
import ru.newsbot.service.impl.ModerateMessageService;
import ru.newsbot.service.impl.PostService;

import javax.annotation.Nullable;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static ru.newsbot.model.enums.ModerationStatusEnum.ACCEPT;
import static ru.newsbot.model.enums.ModerationStatusEnum.REJECT;

@Slf4j
@Service
@AllArgsConstructor
public class TelegramService {
	private final MessageFormatter messageFormatter;
	private final PostService postService;
	private final TelegramBot client;
	private final ChatAdminsFactory chatAdminsFactory;
	private final ModerateMessageService moderateMessageService;

	public SendResponse sendPhoto(Object chatId, String urlPhoto, String caption, @Nullable InlineKeyboardMarkup keyboard) {
		SendPhoto request = new SendPhoto(chatId, urlPhoto)
				.caption(caption)
				.replyMarkup(keyboard);

		return execute(request);
	}

	public SendResponse sendMessage(Object chatId, String text, @Nullable Long postId, @Nullable InlineKeyboardMarkup keyboard) {
		SendMessage request = new SendMessage(chatId, text)
				.withPostId(postId)
				.replyMarkup(keyboard)
				.parseMode(ParseMode.HTML);

		return execute(request);
	}

	public MessagesResponse sendMediaGroup(Object chatId, Post post) {
		InputMediaPhoto[] inputMediaPhoto = new InputMediaPhoto[post.getPostAttachments().size()];
		for (int i = 0; i < post.getPostAttachments().size(); i++) {
			inputMediaPhoto[i] = new InputMediaPhoto(post.getPostAttachments().get(i).getUrlPhoto());
		}
		SendMediaGroup request = new SendMediaGroup(chatId, inputMediaPhoto);
		return execute(request);
	}

	public SendResponse sendDocument(Object chatId, String urlDocument, String caption, InlineKeyboardMarkup keyboard) {
		SendDocument request = new SendDocument(chatId, urlDocument);
		request.caption(caption);
		request.replyMarkup(keyboard);
		return execute(request);
	}

	public SendResponse sendMessage(Post post, Object chatId, InlineKeyboardMarkup keyboard) {
		assertNotNull(chatId, "chatId can not be null");
		SendResponse response;
		if (postService.isPostAsPhotoAlbum(post)) {
			response = sendMessage(chatId, messageFormatter.format(post), post.getId(), keyboard);
			sendMediaGroup(chatId, post);
		} else if (postService.isPostAsGif(post)) {
			response = sendDocument(chatId, post.getPostAttachments().get(0).getUrlPhoto(), post.getTextAsString(), keyboard);
		} else if (postService.isPostAsPhoto(post)) {
			response = sendPhoto(chatId, post.getPostAttachments().get(0).getUrlPhoto(), post.getTextAsString(), keyboard);
		} else {
			response = sendMessage(chatId, messageFormatter.format(post), post.getId(), keyboard);
		}
		return response;
	}

	private <T extends BaseRequest, R extends BaseResponse> R execute(BaseRequest<T, R> request) {
		R response = client.execute(request);
		if (response.isOk()) {
			log.info("Successfully " + messageFormatter.format(request, response));
		} else {
			log.error("Error " + messageFormatter.format(request, response));
		}
		return response;
	}

	/**
	 * Модерирование новостей в агрегаторе (при модерации, новость так же удаляется у других админов)
	 *
	 * @param callbackQuery - событие, которое срабатывает при нажатии на клавиатуру
	 */
	@Retryable(value = SocketTimeoutException.class, maxAttemptsExpression = "${telegram.retry.attemptsCount}",
			backoff = @Backoff(delayExpression = "${telegram.retry.delay}"))
	public void processPressKeyboardInline(CallbackQuery callbackQuery) {
		Integer actorId = callbackQuery.from().id();
		ModerateMessage msg = moderateMessageService.findByTlgrmIdAndAdminId(callbackQuery.message().messageId(), actorId);

		Post post = msg.getPost();
		ModerationStatusEnum moderationStatus = ModerationStatusEnum.from(callbackQuery.data());
		if (moderationStatus == ACCEPT) {
			msg.getPost().setStatus(PostStatusEnum.MODERATED);
		} else if (moderationStatus == REJECT) {
			msg.getPost().setCancelDate(LocalDateTime.now());
			msg.getPost().setStatus(PostStatusEnum.CANCELED);
		}
		postService.save(post);

		Long postId = post.getId();
		List<ChatAdmin> chatAdmins = chatAdminsFactory.findAll(post.getCity());
		List<ModerateMessage> editMessages = chatAdmins.stream()
				.filter(a -> a.getId() != actorId)
				.map(a -> moderateMessageService.findByPostIdAndAdminId(postId, a.getId()))
				.collect(toList());

		editMessages.add(msg);
		editMessages.forEach(m -> {
			deleteMessage(m.getAdminId(), m.getTelegramMessageId());
			m.setProcessedStatus(moderationStatus);
			m.setProcessedTime(LocalDateTime.now());
			moderateMessageService.save(m);
		});

		log.info("{} moderated postId={} with status {}", callbackQuery.from().username(), post.getId(), callbackQuery.data());
	}

	public void deleteMessage(int chatId, int messageId) {
		execute(new DeleteMessage(chatId, messageId));
	}

}
