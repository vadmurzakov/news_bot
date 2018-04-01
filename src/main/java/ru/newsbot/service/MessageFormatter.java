package ru.newsbot.service;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.newsbot.config.factory.ChatAdminsFactory;
import ru.newsbot.config.factory.PublicsFactory;
import ru.newsbot.config.properties.Public;
import ru.newsbot.model.Post;
import ru.newsbot.service.impl.PostService;

import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;

@Component
@AllArgsConstructor
public class MessageFormatter {
    private static final String MSG_FORMAT = "{0}\n\n<i>{1}\nисточник: {2}</i>";
    private static final String MSG_WITH_PHOTO_FORMAT = "{0}\n{1}\n\n<i>{2}\nисточник: {3}</i>";
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final String CALLBACK_QUERY_FORMAT = "`{0} by {1}`\n{2}";
    private static final String BASE_REQUEST_FORMAT = "{0} to {1}";

    private final ChatAdminsFactory chatAdminsFactory;
    private final PublicsFactory publicsFactory;
    private final PostService postService;


	public String format(Post post) {
        Public newsPublic = publicsFactory.findById(post.getPublicId());
        if(postService.isPostWithPhoto(post)) {
        	String urlPhoto = post.getPostAttachments().get(0).getUrlPhoto();
			return MessageFormat.format(MSG_WITH_PHOTO_FORMAT, post.getTextAsString(), urlPhoto, DTF.format(post.getPostDate()), newsPublic.getUrl());
		}
        return MessageFormat.format(MSG_FORMAT, post.getTextAsString(), DTF.format(post.getPostDate()), newsPublic.getUrl());
    }

    public String format(CallbackQuery callbackQuery) {
		Message message = callbackQuery.message();
		return MessageFormat.format(CALLBACK_QUERY_FORMAT, callbackQuery.data(), callbackQuery.from().username(), message.text() != null ? message.text() : message.caption());
    }

    public <T extends BaseRequest, R extends BaseResponse> String format(BaseRequest<T, R> request, R response) {
		String chatId = request.getParameters().get("chat_id").toString().trim();
		String log = MessageFormat.format(BASE_REQUEST_FORMAT, request.getMethod(), chatAdminsFactory.findById(Integer.valueOf(chatId)).getName().toLowerCase());
        if (request.getParameters().get("post_id") != null) log += " , postId = " + request.getParameters().get("post_id");
        if (request.getParameters().get("message_id") != null) log += ", messageId = " + request.getParameters().get("message_id");
        if (response.description() != null) log += ", description: " + response.description();
        return log;
    }

}
