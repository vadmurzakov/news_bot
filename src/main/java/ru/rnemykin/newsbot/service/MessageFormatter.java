package ru.rnemykin.newsbot.service;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.rnemykin.newsbot.config.factory.PublicsFactory;
import ru.rnemykin.newsbot.config.properties.Public;
import ru.rnemykin.newsbot.model.Post;

import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;

@Component
public class MessageFormatter {
    private static final String MSG_FORMAT = "{0}\n\n<i>{1}\nисточник: {2}</i>";
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final String CALLBACK_QUERY_FORMAT = "`{0} by {1}`\n{2}";
    private static final String BASE_REQUEST_FORMAT = "{0} in chatId = {1}";

    @Autowired
    private PublicsFactory publicsFactory;

    public String format(Post post) {
        Public newsPublic = publicsFactory.findById(post.getPublicId());
        return MessageFormat.format(MSG_FORMAT, post.getTextAsString(), DTF.format(post.getPostDate()), newsPublic.getUrl());
    }

    public String format(CallbackQuery callbackQuery) {
        return MessageFormat.format(CALLBACK_QUERY_FORMAT, callbackQuery.data(), callbackQuery.from().username(), callbackQuery.message().text());
    }

    public <T extends BaseRequest, R extends BaseResponse> String format(BaseRequest<T, R> request, R response) {
        String log = MessageFormat.format(BASE_REQUEST_FORMAT, request.getMethod(), request.getParameters().get("chat_id").toString().trim());
        if (request.getParameters().get("post_id") != null) log += " , postId = " + request.getParameters().get("post_id");
        if (request.getParameters().get("message_id") != null) log += ", messageId = " + request.getParameters().get("message_id");
        if (response.description() != null) log += ", description: " + response.description();
        return log;
    }

}
