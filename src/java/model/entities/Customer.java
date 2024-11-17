package model.entities;

import authn.Secured;
import com.google.gson.annotations.Expose;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ivanm
 */
@Entity
@XmlRootElement
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name="Customer_Gen", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Customer_Gen") 
    private Long id;
    @Expose
    private String username;
    private String password;

    @ManyToMany//(fetch = FetchType.LAZY)
    @JoinTable(
        name = "customer_article", 
        joinColumns = @JoinColumn(name = "customer_id"), 
        inverseJoinColumns = @JoinColumn(name = "favarticles_id")
    )
    @Expose
    private List<Article> favArticles;

    @OneToOne//(fetch = FetchType.LAZY, optional = true) 
    @JsonbTransient // Ignorar para evitar bucles en JSON-B
    private Author author;
    
    // Campo para almacenar los links relacionados (HATEOAS)
    @Expose
    private Map<String, String> links = new HashMap<>();

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

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

    @Secured
    public String getPassword() {
        return password;
    }

    @Secured
    public void setPassword(String password) {
        this.password = password;
    }

    public List<Article> getArticles() {
        return favArticles;
    }

    public void setArticles(List<Article> favArticles) {
        this.favArticles = favArticles;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public void setLinks(Map<String, String> links) {
        this.links = links;
    }

    @Override
    public String toString() {
        return "Customer{" + "id=" + id + ", username=" + username + ", favArticles=" + favArticles + "}";
    }
}
