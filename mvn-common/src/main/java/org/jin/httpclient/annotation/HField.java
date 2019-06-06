package org.jin.httpclient.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jin.httpclient.bytesAdapter.BytesAdapter;
import org.jin.httpclient.enums.HFieldModel;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface HField {

	/**
	 * family
	 * 
	 * @return
	 */
	String f() default "a";

	/**
	 * qualifier
	 * 
	 * @return
	 */
	String q();
	
	boolean timestamp() default false;

	HFieldModel mode() default HFieldModel.DEFAULT;

	Class<? extends BytesAdapter> adapter() default BytesAdapter.class;
	
	/**
	 * 如果值为null或者为默认值，是否插入，默认为否
	 * @return
	 */
	boolean saveIfNullOrDefault() default false;
	

}
