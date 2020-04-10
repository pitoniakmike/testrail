/*

 * Copyright (C) 2002 by Michael Pitoniak (pitoniakm@msn.com)
 * All rights are reserved.
 * Reproduction and/or redistribution in whole or in part is expressly
 * prohibited without the written consent of the copyright owner.
 *
 * This Software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

package utils.gson;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.lang.SerializationUtils;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import utils.datetime.DateTimeServices;


public class GsonServices implements Serializable, Cloneable{
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(new Throwable().getStackTrace()[0].getClassName());
	

	public static GsonServices build() {
	    return new GsonServices();
	}
	
	private GsonServices() {}
	
	public String prettyPrint(String jSONObject){
		return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(new JsonParser().parse(jSONObject));
	}
	
	public String prettyPrint( JsonElement jsonElement){
		return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(new JsonParser().parse(jsonElement.toString()));
	}

	public String prettyPrint( org.json.simple.JSONObject jSONObject){
		return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(new JsonParser().parse(jSONObject.toString()));
	}
	
	public String prettyPrint( JsonObject jsonObject){
		return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(new JsonParser().parse(jsonObject.toString()));
	}
	
	public String prettyPrint(JSONArray jSONArray){
		return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(new JsonParser().parse(jSONArray.toString()));
	} 
	
	public String prettyPrint(org.json.JSONArray jSONArray){
		return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(new JsonParser().parse(jSONArray.toString()));
	} 
	
	public String prettyPrint(JsonArray jsonArray){
		LOGGER.debug("{}", jsonArray);
		return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(new JsonParser().parse(jsonArray.toString()));
	}
	
	public String toJson(Object object){
		return toJson(object, false);
	}
	
	public Object fromJson(String json, Object obj){
		GsonBuilder gsonBuilder =  new GsonBuilder()
        .disableHtmlEscaping()
        .setDateFormat(DateTimeServices.BASIC_DATE_TIME_FORMAT)
        .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
        .serializeNulls();
		return gsonBuilder.create().fromJson(json, obj.getClass());
	}
	
	public <T> T fromJson(String json, Class<T> clz){
	    return new Gson().fromJson(json, clz);
	}
	
	public <T> JsonElement toJsonElement(T obj) {
		Gson gson = new GsonBuilder().setDateFormat(DateTimeServices.BASIC_DATE_TIME_FORMAT).create();
		return gson.toJsonTree(obj);
	}
	
	public <T> JsonObject toJsonObject(T obj) {
		return (JsonObject) toJsonElement(obj);
	}
	
	public JsonObject toJsonObject(String json) {
		return (JsonObject) new JsonParser().parse(json);
	}
	
	public JsonElement toJsonElement(String json) {
		return new JsonParser().parse(json);
	}

	
	public String toJson(Object object, boolean prettyPrint){
		GsonBuilder gsonBuilder =  new GsonBuilder()
            .disableHtmlEscaping()
            .setDateFormat(DateTimeServices.BASIC_DATE_TIME_FORMAT)
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .serializeNulls();
            if(prettyPrint ) {
            	gsonBuilder.setPrettyPrinting();
            }
		return gsonBuilder.create().toJson(object);
	}
	
	public String toAnsibleObject(Object object){
		Gson gson = new GsonBuilder()
        .disableHtmlEscaping()
        .setDateFormat(DateTimeServices.BASIC_DATE_TIME_FORMAT)
        .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
        .serializeNulls()
        .create();
		 
		return gson.toJson(object).replaceAll("\"", "'");
	}
	
	public String toAnsibleObject(Object object, Type type){
        Gson gson = new GsonBuilder()
        .disableHtmlEscaping()
        .setDateFormat(DateTimeServices.BASIC_DATE_TIME_FORMAT)
        .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
        .serializeNulls()
        .create();
         
        return gson.toJson(object, type).replaceAll("\"", "'");
    }
	
	// [
	//public static <T> void addToList (List<T> listArg, T... elements) {
	//@SuppressWarnings({"unchecked", "varargs"})
	@SuppressWarnings(value = { "" })
	public <T> String arrayOfObject(List<T> list, T... elements) {
		for(T element : elements) {
			list.add(element);
		}
		Gson gson = new Gson(); 
		String json = gson.toJson(list);
		
		return json;
	}
	
	@Override
    protected GsonServices clone() throws CloneNotSupportedException{
        return (GsonServices)SerializationUtils.clone(this);
    }

}
