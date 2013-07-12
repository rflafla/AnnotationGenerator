/*
 * Copyright 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package fr.flafla.generator.access;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor6;

/**
 * Utility type for reducing complex type declarations to ones suitable for
 * determining assignability based on RequestFactory's type-mapping semantics.
 * <p>
 * Rules:
 * <ul>
 * <li>primitive type {@code ->} boxed type (optional)</li>
 * <li>{@code void -> Void} (optional)</li>
 * <li>{@code <T extends Foo> -> Foo}</li>
 * <li>{@code ? extends Foo -> Foo}</li>
 * <li>{@code Foo<complex type> -> Foo<simplified type>}</li>
 * </ul>
 */
public class TypeSimplifier extends SimpleTypeVisitor6<TypeMirror, Environment> {

  public static TypeMirror simplify(TypeMirror toBox, boolean boxPrimitives, Environment state) {
    if (toBox == null) {
      return null;
    }
    return toBox.accept(new TypeSimplifier(boxPrimitives), state);
  }

  private final boolean boxPrimitives;

  private TypeSimplifier(boolean boxPrimitives) {
    this.boxPrimitives = boxPrimitives;
  }

  @Override
  public TypeMirror visitDeclared(DeclaredType x, Environment state) {
    if (x.getTypeArguments().isEmpty()) {
      return x;
    }
    List<TypeMirror> newArgs = new ArrayList<TypeMirror>(x.getTypeArguments().size());
    for (TypeMirror original : x.getTypeArguments()) {
      // Are we looking at a self-parameterized type like Foo<T extends Foo<T>>?
      if (original.getKind().equals(TypeKind.TYPEVAR) && Environment.get().getTypeUtils().isAssignable(original, x)) {
        // If so, return a raw type
        return Environment.get().getTypeUtils().getDeclaredType((TypeElement) x.asElement());
      } else {
        newArgs.add(original.accept(this, state));
      }
    }
    return Environment.get().getTypeUtils().getDeclaredType((TypeElement) x.asElement(), newArgs
        .toArray(new TypeMirror[newArgs.size()]));
  }

  @Override
  public TypeMirror visitNoType(NoType x, Environment state) {
    if (boxPrimitives) {
      return state.findType(Void.class);
    }
    return x;
  }

  @Override
  public TypeMirror visitPrimitive(PrimitiveType x, Environment state) {
    if (boxPrimitives) {
      return Environment.get().getTypeUtils().boxedClass(x).asType();
    }
    return x;
  }

  @Override
  public TypeMirror visitTypeVariable(TypeVariable x, Environment state) {
//    if (x.equals(x.getUpperBound())) {
//      // See comment in TransportableTypeVisitor
////      return Environment.get().getTypeUtils().erasure(x);
//      return x.asElement().;
//    }
    return x.getUpperBound().accept(this, state);
  }

  @Override
  public TypeMirror visitWildcard(WildcardType x, Environment state) {
    return Environment.get().getTypeUtils().erasure(x).accept(this, state);
  }

  @Override
  protected TypeMirror defaultAction(TypeMirror x, Environment state) {
    return Environment.get().getTypeUtils().getNoType(TypeKind.NONE);
  }
}