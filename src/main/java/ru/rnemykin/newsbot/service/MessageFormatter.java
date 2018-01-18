package ru.rnemykin.newsbot.service;

import org.springframework.stereotype.Component;
import ru.rnemykin.newsbot.model.Post;

import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;

@Component
public class MessageFormatter {
    private static final String MSG_FORMAT = "{0}\n\n<i>{1}\nисточник: {2}</i>";
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");


    public String format(Post post) {
        return MessageFormat.format(MSG_FORMAT, post.getTextAsString(), DTF.format(post.getPostDate()), post.getPostPublic().url());
    }
}
