package fr.flafla.generator.access;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import com.google.common.collect.Lists;

public class AbstractElement<T extends Element> {
	protected final T elt;

	public AbstractElement(T elt) {
		assert elt != null;
		this.elt = elt;
	}

	public boolean has(Class<? extends Annotation> annotation) {
		return elt != null && elt.getAnnotation(annotation) != null;
	}

	public <A extends Annotation> fr.flafla.generator.access.Annotation<A> get(Class<A> annotation) {
		final List<? extends AnnotationMirror> annotationMirrors = elt.getAnnotationMirrors();
		for (final AnnotationMirror mirror : annotationMirrors) {
			if (mirror.getAnnotationType().asElement().toString().equals(annotation.getName())) {
				return new fr.flafla.generator.access.Annotation<A>(mirror);
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Iterable<fr.flafla.generator.access.Annotation<?>> getAnnotations() {
		final List<fr.flafla.generator.access.Annotation<?>> result = Lists.newArrayList();
		final List<? extends AnnotationMirror> annotations = elt.getAnnotationMirrors();
		for (final AnnotationMirror annotation : annotations) {
			result.add(new fr.flafla.generator.access.Annotation(annotation));
		}
		return result;
	}
}
