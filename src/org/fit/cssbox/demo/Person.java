package org.fit.cssbox.demo;

public class Person {
	String name;
	Integer x;
	Integer y;
	Integer width;
	Integer heigth;
	String tag;
    String parent;
    String color;
	Person(String name, Integer x, Integer y,String tag,Integer width,Integer heigth,String parent) 
	{
		this.name=name;
		this.x=x;
		this.y=y;
		this.tag=tag;
		this.width=width;
		this.heigth=heigth;
		this.parent=parent;
	}

}
