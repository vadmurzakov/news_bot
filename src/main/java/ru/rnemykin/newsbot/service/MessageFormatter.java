package ru.rnemykin.newsbot.service;

import org.springframework.stereotype.Component;
import ru.rnemykin.newsbot.model.Post;

import java.text.MessageFormat;

@Component
public class MessageFormatter {
    private static final String MSG_FORMAT = "{0}\n\n{1}";

    public String format(Post post) {
        return MessageFormat.format(MSG_FORMAT, post.getTextAsString(), post.getPostPublic().url());
    }
}
