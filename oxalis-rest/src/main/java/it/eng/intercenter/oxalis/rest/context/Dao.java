package it.eng.intercenter.oxalis.rest.context;

import java.util.List;

/**
 * 
 * @author Manuel Gozzi
 *
 * @param <T>
 */
public interface Dao<T> {

	List<? extends T> getAll();
	
	T getById(String id);
	
}
