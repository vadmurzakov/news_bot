package ru.rnemykin.newsbot.service;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.friends.responses.GetResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.vkontakte.api.VKontakte;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VkService {

    private final ConnectionRepository repository;
    private VKontakte vkontakte;

    @Autowired
    public VkService(ConnectionRepository repository) {
        this.repository = repository;
    }

    @SneakyThrows
    @PostConstruct
    void init() {
        Connection<VKontakte> connection = repository.findPrimaryConnection(VKontakte.class);
        vkontakte = connection.getApi();
        VkApiClient vk = new VkApiClient(HttpTransportClient.getInstance());
        GetResponse userIds = vk.friends().get(vkontakte.getUserActor()).execute();
        List<String> ids = userIds.getItems().stream().map(Object::toString).collect(Collectors.toList());
    }
}
