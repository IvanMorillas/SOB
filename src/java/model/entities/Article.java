package model.entities;

import com.google.gson.annotations.Expose;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ivanm
 */
@NamedQueries({
    @NamedQuery(name="findAllArticles",
                query="SELECT a FROM Article a ORDER BY a.views DESC"),
    @NamedQuery(name="findArticlesByAuthor",
                query="SELECT a FROM Article a WHERE a.author.username = :author ORDER BY a.views DESC"),
    @NamedQuery(name="findArticlesByOneTopic",
                query="SELECT a FROM Article a JOIN a.topics t WHERE t.name = :topic ORDER BY a.views DESC"),
    @NamedQuery(name="findArticlesByTwoTopics",
            query="SELECT a FROM Article a WHERE " +
                  "EXISTS (SELECT t FROM a.topics t WHERE t.name = :topic1) AND " +
                  "EXISTS (SELECT t FROM a.topics t WHERE t.name = :topic2) " +
                  "ORDER BY a.views DESC"),
    @NamedQuery(name="findArticlesByOneTopicAndAuthor",
                query="SELECT a FROM Article a JOIN a.topics t WHERE t.name = :topic AND a.author.username = :author ORDER BY a.views DESC"),
    @NamedQuery(name="findArticlesByTwoTopicsAndAuthor",
                query = "SELECT a FROM Article a " +
                    "WHERE EXISTS (SELECT t FROM a.topics t WHERE t.name = :topic1) " +
                    "AND EXISTS (SELECT t FROM a.topics t WHERE t.name = :topic2) " +
                    "AND a.author.username = :author " +
                    "ORDER BY a.views DESC"),
    @NamedQuery(name="findArticleById", 
                query="SELECT a FROM Article a WHERE a.id = :id")
})                 
@Entity
@XmlRootElement
public class Article implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name="Article_Gen", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Article_Gen") 
    private Long id;
    @Expose
    private String title;
    @Expose
    private Date date;

    private float Numviews;
    @Expose
    private String views;
    @Expose
    private String summary;
    @Expose
    private String text;
    @Expose
    private boolean isPrivate;
    
    @ManyToOne
    @JsonbTransient
    @Expose
    private Customer author;
    
    @ManyToMany
    @JoinTable(
        name = "article_topic",
        joinColumns = @JoinColumn(name = "article_id"),
        inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    @JsonbTransient
    @Expose
    private List<Topic> topics = new ArrayList<>();
    
    public static long getSerialVersionUID(){
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getNumViews() {
        return Numviews;
    }
    
    public String getViews(){
        return views;
    }

    public void setViews(float Numviews) {
        this.views = formatViews(Numviews);
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public float getNumviews() {
        return Numviews;
    }

    public void setNumviews(float Numviews) {
        this.Numviews = Numviews;
    }

    public Customer getAuthor() {
        return author;
    }

    public void setAuthor(Customer author) {
        this.author = author;
    }
       
    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(Topic topic) {
        topics.add(topic);
    }

    // Método privado para dar formato a las vistas
    private String formatViews(float views) {
        if (views < 1000) {
            return String.valueOf((int) views);
        } else if (views < 1000000) {
            return String.format("%.1fk", views / 1000);
        } else {
            return String.format("%.1fM", views / 1000000);
        }
    }

    @Override
    public String toString() {
        return "Article{" + "id=" + id + ", title=" + title + ", date=" +
                date + ", views=" + views + ", summary=" + summary + 
                ", text=" + text + ", isPrivate=" + isPrivate + ", author2=" + 
                author + ", topics=" + topics + '}';
    }
}
