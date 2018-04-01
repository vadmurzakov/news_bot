package ru.newsbot.model;

import org.springframework.data.domain.Persistable;

import java.io.Serializable;

public abstract class Model<ID extends Serializable> implements Persistable<ID> {
	@Override
	public boolean isNew() {
		return getId() == null;
	}
}
