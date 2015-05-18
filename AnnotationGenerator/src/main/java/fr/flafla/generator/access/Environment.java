package fr.flafla.generator.access;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

public class Environment {
	private static ThreadLocal<ProcessingEnvironment> envs = new ThreadLocal<ProcessingEnvironment>();
	private static ThreadLocal<Messager> messagers = new ThreadLocal<Messager>();

	public static void set(ProcessingEnvironment env, Messager messager) {
		envs.set(env);
		messagers.set(messager);
	}

	public static Messager getMessager() {
		return messagers.get();
	}
	
	public static ProcessingEnvironment get() {
		return envs.get();
	}

	/**
	 * Utility method to look up raw types from class literals.
	 */
	public DeclaredType findType(Class<?> clazz) {
		return get().getTypeUtils().getDeclaredType(get().getElementUtils().getTypeElement(clazz.getCanonicalName()));
	}
	
	@SuppressWarnings("unchecked")
	public static <H extends Element> H asElement(TypeMirror type) {
		return (H) get().getTypeUtils().asElement(type);
	}
}
