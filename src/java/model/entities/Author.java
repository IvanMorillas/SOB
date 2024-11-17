/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.entities;

import com.google.gson.annotations.Expose;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author ivanm
 */
@Entity
@XmlRootElement
public class Author implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name="Author_Gen", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Author_Gen") 
    private Long id;
    @Expose
    private String username;
    
    @OneToMany(mappedBy="author"/*, fetch = FetchType.LAZY*/)
    //@Expose(serialize = true)
    List<Article> articles;
    
    @OneToOne(mappedBy="author")
    private Customer customer;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @Override
    public String toString() {
        return "Author{" + "id=" + id + ", username=" + username + ", articles=" + articles + '}';
    }

}
