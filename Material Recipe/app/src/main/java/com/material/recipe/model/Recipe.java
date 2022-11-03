package com.material.recipe.model;

import java.io.Serializable;

public class Recipe implements Serializable{
    public Integer id;
    public String name;
    public String instruction;
    public Integer duration;
    public String image;
    public Integer category;
    public String category_name;
    public Long date_create;
}
