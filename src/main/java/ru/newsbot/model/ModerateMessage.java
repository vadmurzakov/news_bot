package ru.newsbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.newsbot.model.enums.ModerationStatusEnum;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Table
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerateMessage extends Model<Long> {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Long postId;
	private Integer adminId;
	private Integer telegramMessageId;
	private LocalDateTime processedTime;

	@Enumerated(EnumType.STRING)
	private ModerationStatusEnum processedStatus;

	@OneToOne
	@JoinColumn(name = "postId", insertable = false, updatable = false)
	private Post post;
}
