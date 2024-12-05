package service;

import authn.Secured;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.xml.messaging.saaj.util.Base64;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.entities.Article;
import model.entities.Topic;
import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
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
                                return f.getName().equals("text") || f.getName().equals("topics") || f.getName().equals("links");
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
                                return f.getName().equals("isPrivate") || f.getName().equals("summary") || f.getName().equals("links");
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
            String json = gsonList.toJson(articles);
            return Response.ok(json).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Articles not found").build();
        }
    }

    @GET
    @Secured
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response findArticle(@PathParam("id") Long id) {
        Article article = super.find(id);

        if (article != null) {
            // Convierte el objeto `Article` a JSON
            String json = gsonDetail.toJson(article);
            return Response.ok(json).build();
        }else{
            return Response.status(Response.Status.NOT_FOUND).entity("Article not found").build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Secured
    public Response deleteArticle(@PathParam("id") Long id, @HeaderParam("Authorization") String authHeader) {
        if (authHeader == null || authHeader.isEmpty() || !authHeader.startsWith("Basic ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Authorization header is missing or invalid")
                    .build();
        }

        // Decodificar el encabezado Authorization
        String username;
        try {
            String encodedCredentials = authHeader.replace("Basic ", "");
            String decodedCredentials = Base64.base64Decode(encodedCredentials);
            username = decodedCredentials.split(":")[0]; // Asumimos formato "username:password"
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid authorization header format")
                    .build();
        }

        // Buscar el artículo
        Article article = super.find(id);
        if (article == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Article not found")
                    .build();
        }

        // Verificar si el usuario autenticado es el autor del artículo
        if (!article.getAuthor().getUsername().equals(username)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Only the author can delete this article")
                    .build();
        }

        // Eliminar el artículo
        super.remove(article);
        return Response.noContent().build();
    }


    @POST
    @Secured
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response postArticle(String article, @HeaderParam("Authorization") String authHeader) {
        // Validar que el encabezado Authorization no esté vacío
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Authorization header is missing or invalid.")
                    .build();
        }

        // Extraer y decodificar el username desde authHeader
        String encodedCredentials = authHeader.replace("Basic ", "");
        String decodedCredentials = Base64.base64Decode(encodedCredentials);
        String username = decodedCredentials.split(":")[0]; // Asumimos formato "username:password"

        // Convertir el JSON a un objeto Article temporal
        Gson gson = new Gson();
        Article temp = gson.fromJson(article, Article.class);

        // Crear el objeto Article resultante y asignar fecha de publicación
        Article result = new Article();
        result.setDate(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        result.setTitle(temp.getTitle());

        // Validar el número de palabras en summary
        if (temp.getSummary() != null && temp.getSummary().split("\\s+").length > 20) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Summary exceeds the maximum limit of 20 words.")
                    .build();
        } else {
            result.setSummary(temp.getSummary());
        }

        // Validar el número de palabras en text
        if (temp.getText() != null && temp.getText().split("\\s+").length > 500) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Text exceeds the maximum limit of 500 words.")
                    .build();
        } else {
            result.setText(temp.getText());
        }

        result.setViews(0);
        result.setIsPrivate(temp.isPrivate());

        // Buscar el Customer correspondiente al username
        TypedQuery<Customer> tq = em.createQuery(
                "SELECT c FROM Customer c WHERE c.username = :username", Customer.class);
        tq.setParameter("username", username);

        Customer customer;
        try {
            customer = tq.getSingleResult(); // Intentamos obtener al usuario
        } catch (NoResultException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("User not found.")
                    .build();
        }

        // Asignar el autor al artículo
        result.setAuthor(customer);

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

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
