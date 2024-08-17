package com.example.hometask.api;

public class ApiResponse<T> {
    private T data;
    private int page;
    private int per_page;
    private int total;
    private int total_pages;

    // Getters and setters
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getPerPage() { return per_page; }
    public void setPerPage(int per_page) { this.per_page = per_page; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getTotalPages() { return total_pages; }
    public void setTotalPages(int total_pages) { this.total_pages = total_pages; }
}