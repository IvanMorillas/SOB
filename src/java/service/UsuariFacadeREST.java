package service;

import authn.Secured;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.entities.Article;
import model.entities.Customer;

/**
 *
 * @author ivanm
 */
@Stateless
@Path("/rest/api/v1/customer")
public class UsuariFacadeREST extends AbstractFacade<Customer> {
    
    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em; 
   
    public UsuariFacadeREST(){
        super(Customer.class);
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getCustomers() {
        List<Customer> customers = super.findAll();
        if (customers != null && !customers.isEmpty()) {
            // Iterar sobre cada cliente para agregar el enlace del último artículo si es un autor
            for (Customer customer : customers) {
                // Verificar si el customer tiene rol de autor y si tiene artículos
                if (customer.getAuthor() != null && customer.getAuthor().getArticles() != null && 
                    !customer.getAuthor().getArticles().isEmpty()) {
                    // Obtener el último artículo publicado
                    Article latestArticle = customer.getAuthor().getArticles()
                                                    .stream()
                                                    .max((a1, a2) -> a1.getDate().compareTo(a2.getDate()))
                                                    .orElse(null);
                    if (latestArticle != null) {
                        Map<String, String> links = new HashMap<>();
                        links.put("article", "/article/" + latestArticle.getId());
                        customer.setLinks(links);
                    }
                }
            }
            // Convertir la lista de clientes a JSON usando Gson
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = gson.toJson(customers);
            return Response.ok(json).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("No users found").build();
        }
    }
    
    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getCustomer(@PathParam("id") Long id) {
        Customer customer = super.find(id);
        if (customer != null) {
            // Verificar si el customer tiene rol de autor y si tiene artículos
            if (customer.getAuthor() != null && customer.getAuthor().getArticles() != null && 
                !customer.getAuthor().getArticles().isEmpty()) {
                // Obtener el último artículo publicado
                Article latestArticle = customer.getAuthor().getArticles()
                                                .stream()
                                                .max((a1, a2) -> a1.getDate().compareTo(a2.getDate()))
                                                .orElse(null);
                if (latestArticle != null) {
                    Map<String, String> links = new HashMap<>();
                    links.put("article", "/article/" + latestArticle.getId());
                    customer.setLinks(links);
                }
            }
            // Convertir la lista de clientes a JSON usando Gson
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = gson.toJson(customer);
            return Response.ok(json).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("No users found").build();
        }
    }
    
    @PUT
    //@Secured
    @Path("/{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response putCustomer(@PathParam("id") Long id, Customer customer) {
        Customer existingCustomer = super.find(id);
        if (existingCustomer == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No user found").build();
        } else {
            existingCustomer.setUsername(customer.getUsername());
            // No actualizar la contraseña aquí por seguridad
            return Response.ok(existingCustomer, MediaType.APPLICATION_JSON).build();
        }
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
