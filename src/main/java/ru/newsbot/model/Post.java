package ru.newsbot.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.codec.CharEncoding;
import ru.newsbot.model.enums.CityEnum;
import ru.newsbot.model.enums.PostStatusEnum;

import javax.persistence.*;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Table
@Entity
@EqualsAndHashCode(callSuper = true)
public class Post extends Model<Long> {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private CityEnum city;

	@Enumerated(EnumType.STRING)
	private PostStatusEnum status;

	private Integer publicId;
	private LocalDateTime createDate;
	private LocalDateTime sentDate;
	private LocalDateTime cancelDate;
	private LocalDateTime publishDate;
	private Integer sentAttemptsCount;

	private Long postId;
	private Long ownerId;
	private String type;

	@Lob
	private byte[] text;
	private long likesCount;
	private long viewsCount;
	private long repostsCount;
	private long commentsCount;
	private Boolean isPinned;
	private LocalDateTime postDate;

	@OneToMany(fetch = FetchType.EAGER)
	@JoinColumn(name = "postId", insertable = false, updatable = false)
	private List<PostAttachment> postAttachments = new ArrayList<>();

	public String getTextAsString() {
		return new String(text, Charset.forName(CharEncoding.UTF_8));
	}

	@PrePersist
	void onInsert() {
		sentAttemptsCount = 0;
		createDate = LocalDateTime.now();
	}
}

