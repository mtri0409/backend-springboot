package com.phamdinhminhtri.example05.service;


import com.phamdinhminhtri.example05.entity.Category;
import com.phamdinhminhtri.example05.payloads.CategoryDTO;
import com.phamdinhminhtri.example05.payloads.CategoryResponse;

public interface CategoryService {

    CategoryDTO createCategory(Category category);
    CategoryResponse getCategories(Integer pageNumber ,Integer pageSize,String sortBy,String sortOrder);
    CategoryDTO updateCategory(Category category,Long categoryId);
    String deleteCategory(Long categoryId);
    CategoryDTO getCategoryById(Long categoryId);
} 

