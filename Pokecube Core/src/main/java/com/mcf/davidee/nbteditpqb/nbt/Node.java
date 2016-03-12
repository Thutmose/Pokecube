package com.mcf.davidee.nbteditpqb.nbt;

import java.util.ArrayList;
import java.util.List;

public class Node<T> {

	private List<Node<T>> children;
	
	private Node<T> parent;
	private T obj;
	
	private boolean drawChildren;
	
	public Node(){
		this((T)null);
	}
	
	public Node(Node<T> parent){
		this(parent,null);
	}
	
	public Node(Node<T> parent, T obj){
		this.parent = parent;
		children = new ArrayList<>();
		this.obj = obj;
	}
	
	public Node(T obj){
		children = new ArrayList<>();
		this.obj = obj;
	}
	
	public void addChild(Node<T> n){
		children.add(n);
	}
	
	public List<Node<T>> getChildren(){
		return children;
	}
	
	public T getObject(){
		return obj;
	}
	
	public Node<T> getParent(){
		return parent;
	}
	
	public boolean hasChildren() {
		return children.size() > 0;
	}
	
	public boolean hasParent(){
		return parent != null;
	}
	
	public boolean removeChild(Node<T> n){
		return children.remove(n);
	}
	
	public void setDrawChildren(boolean draw){
		drawChildren = draw;
	}

	public boolean shouldDrawChildren(){
		return drawChildren;
	}
	
	@Override
    public String toString(){
		return "" + obj;
	}

	
}
