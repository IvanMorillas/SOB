package service;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import model.entities.Article;
import model.entities.Topic;
import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import model.entities.Author;
import model.entities.Customer;

@Stateless
@Path("/rest/api/v1/article")
public class ArticleFacadeREST extends AbstractFacade<Article> {
    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;

    public ArticleFacadeREST() {
        super(Article.class);
    }
    
    // Configuración para vista de listado (excludeFieldsWithoutExposeAnnotation solo para los campos del listado)
    Gson gsonList = new GsonBuilder()
                        .excludeFieldsWithoutExposeAnnotation()
                        .addSerializationExclusionStrategy(new ExclusionStrategy() {
                            @Override
                            public boolean shouldSkipField(FieldAttributes f) {
                                // Excluye campos que no necesites en el listado
                                return f.getName().equals("text") || f.getName().equals("topics");
                            }
                            @Override
                            public boolean shouldSkipClass(Class<?> clazz) {
                                return false;
                            }
                        })
                        .create();

    // Configuración para vista de detalle (sin excluir campos adicionales)
    Gson gsonDetail = new GsonBuilder()
                        .excludeFieldsWithoutExposeAnnotation()
                        .addSerializationExclusionStrategy(new ExclusionStrategy() {
                            @Override
                            public boolean shouldSkipField(FieldAttributes f) {
                                // Excluye campos que no necesites en el listado
                                return f.getName().equals("isPrivate") || f.getName().equals("summary");
                            }
                            @Override
                            public boolean shouldSkipClass(Class<?> clazz) {
                                return false;
                            }
                        })
                        .create();


    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getArticles(
            @QueryParam("topic") List<String> topics,
            @QueryParam("author") String author) {

        List<Article> articles;

        if ((topics == null || topics.isEmpty()) && (author == null || author.isEmpty())) {
            // Caso 1: Sin filtros - devolver todos los artículos
            articles = em.createNamedQuery("findAllArticles", Article.class).getResultList();
        } else if (topics != null && topics.size() == 1 && (author == null || author.isEmpty())) {
            // Caso 2: Filtrar por un solo tema
            articles = em.createNamedQuery("findArticlesByOneTopic", Article.class)
                         .setParameter("topic", topics.get(0))
                         .getResultList();
        } else if (topics != null && topics.size() == 2 && (author == null || author.isEmpty())) {
            // Filtrar por dos temas
            articles = em.createNamedQuery("findArticlesByTwoTopics", Article.class)
                         .setParameter("topic1", topics.get(0))
                         .setParameter("topic2", topics.get(1))
                         .getResultList();
        }
         else if (author != null && !author.isEmpty() && (topics == null || topics.isEmpty())) {
            // Caso 4: Filtrar solo por autor
            articles = em.createNamedQuery("findArticlesByAuthor", Article.class)
                         .setParameter("author", author)
                         .getResultList();
        } else if (author != null && !author.isEmpty() && topics != null && topics.size() == 1) {
            // Caso 5: Filtrar por un tema y un autor
            articles = em.createNamedQuery("findArticlesByOneTopicAndAuthor", Article.class)
                         .setParameter("topic", topics.get(0))
                         .setParameter("author", author)
                         .getResultList();
        } else if (author != null && !author.isEmpty() && topics != null && topics.size() == 2) {
            // Caso 6: Filtrar por dos temas y un autor
            articles = em.createNamedQuery("findArticlesByTwoTopicsAndAuthor", Article.class)
                         .setParameter("topic1", topics.get(0))
                         .setParameter("topic2", topics.get(1))
                         .setParameter("author", author)
                         .getResultList();
        } else {
            // Si los parámetros no son válidos (por ejemplo, más de dos temas), retornar un error
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Invalid parameters: Only up to two topics are allowed.")
                           .build();
        }

        if (articles != null && !articles.isEmpty()) {
            //Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = gsonList.toJson(articles);
            return Response.ok(json).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Articles not found").build();
        }
    }

    @GET
    //@Secured
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findArticle(@PathParam("id") Long id/*, @HeaderParam("Authorization") String token*/) {
        if(super.find(id) == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Article not found").build();
        } else {
            Article temp = super.find(id);
            //Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = gsonDetail.toJson(temp);
            return Response.ok(json).build();
        }
    }

    @DELETE
    @Path("/{id}")
    //@Secured
    public Response deleteArticle(@PathParam("id") Long id, @HeaderParam("Authorization") String token) {
        Article article = super.find(id);
        if (article == null) {
            return Response.status(Status.NOT_FOUND).entity("Article not found").build();
        }
        
        /*if (!isUserAuthor(token, article)) {
            return Response.status(Status.FORBIDDEN).entity("Only the author can delete this article").build();
        }*/
        
        super.remove(article);
        return Response.noContent().build();
    }

    @POST
    //@Secured
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response postArticle(String article) {
        // Convertir el JSON a un objeto Article temporal
        Gson gson = new Gson();
        Article temp = gson.fromJson(article, Article.class);

        // Crear el objeto Article resultante y asignar fecha de publicación
        Article result = new Article();
        result.setDate(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        result.setTitle(temp.getTitle());
        result.setSummary(temp.getSummary());
        result.setText(temp.getText());
        result.setViews(temp.getViews());
        result.setIsPrivate(temp.isPrivate());

        // Verificar y asignar autor usando authorUsername
        TypedQuery<Customer> tq = em.createQuery(
            "SELECT c FROM Customer c WHERE c.username = :username", Customer.class);
        tq.setParameter("username", temp.getAuthor().getUsername());
        Customer customer = tq.getSingleResult();
        if (customer == null || customer.getAuthor() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Author not found or invalid")
                    .build();
        }
        result.setAuthor(customer.getAuthor());

        // Agregar los tópicos válidos
        for (Topic topic : temp.getTopics()) {
            TypedQuery<Topic> topicQuery = em.createQuery(
                "SELECT t FROM Topic t WHERE t.name = :name", Topic.class);
            topicQuery.setParameter("name", topic.getName());
            Topic validTopic = topicQuery.getSingleResult();
            if (validTopic != null) {
                result.getTopics().add(validTopic);
                validTopic.getArticles().add(result);
            }
        }

        // Persistir el nuevo artículo
        super.create(result);

        // Retornar respuesta 201 con el id del nuevo artículo
        return Response.status(Response.Status.CREATED)
                       .entity("{\"id\":" + result.getId() + "}")
                       .build();
    }


    private boolean validateTopics(List<Topic> topics) {
        for (Topic topic : topics) {
            if (em.find(Topic.class, topic.getId()) == null) {
                return false;
            }
        }
        return true;
    }

    private Author getAuthenticatedUser(String token) {
        // Lógica para obtener el usuario autenticado usando el token.
        return em.find(Author.class, token);  // Solo es un ejemplo; implementa el método real.
    }

    /*private boolean isUserAuthenticated(String token) {
        // Lógica de autenticación usando el token
        return getAuthenticatedUser(token) != null;
    }

    private boolean isUserAuthor(String token, Article article) {
        Author author = getAuthenticatedUser(token);
        return author != null && author.equals(article.getAuthor());
    }*/

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
