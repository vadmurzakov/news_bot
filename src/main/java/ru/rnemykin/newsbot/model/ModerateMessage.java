package ru.rnemykin.newsbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.rnemykin.newsbot.model.enums.ModerationStatusEnum;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@Table
//@Entity
public class ModerateMessage {
    private Long postId;
    private Integer adminId;
    private Integer telegramMessageId;
    private LocalDateTime processedTime;

//    @Enumerated(EnumType.STRING)
    private ModerationStatusEnum processedStatus;
}
