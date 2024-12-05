package authn;

import com.sun.xml.messaging.saaj.util.Base64;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.StringTokenizer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.NoResultException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import jakarta.annotation.Priority;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.container.ResourceInfo;

@Priority(Priorities.AUTHENTICATION)
@Provider
public class RESTRequestFilter implements ContainerRequestFilter {
    private static final String AUTHORIZATION_HEADER_PREFIX = "Basic ";

    @Context
    private ResourceInfo resourceInfo;

    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;

    @Override
    public void filter(ContainerRequestContext requestCtx) throws IOException {
        Method method = resourceInfo.getResourceMethod();
        if (method != null) {
            Secured secured = method.getAnnotation(Secured.class);
            if (secured != null) {
                // Verifica si la solicitud es para el método "findArticleById"
                if (method.getName().equals("findArticle")) {
                    // Obtener el ID del artículo de la URL
                    String path = requestCtx.getUriInfo().getPath();
                    String[] pathSegments = path.split("/");
                    Long articleId;
                    try {
                        articleId = Long.parseLong(pathSegments[pathSegments.length - 1]);
                    } catch (NumberFormatException e) {
                        requestCtx.abortWith(Response.status(Response.Status.BAD_REQUEST).build());
                        return;
                    }

                    // Verificar si el artículo es público o privado
                    boolean isPrivate;
                    try {
                        isPrivate = em.createQuery(
                            "SELECT a.isPrivate FROM Article a WHERE a.id = :id", Boolean.class)
                            .setParameter("id", articleId)
                            .getSingleResult();
                    } catch (NoResultException e) {
                        requestCtx.abortWith(Response.status(Response.Status.NOT_FOUND).build());
                        return;
                    }

                    // Si el artículo es público, permitir acceso sin autenticación
                    if (!isPrivate) {
                        return; // Permitir continuar sin autenticación
                    }

                    // Si el artículo es privado, realizar autenticación
                    if (!authenticateUser(requestCtx)) {
                        return;
                    }
                } else {
                    // Para otros métodos que requieran autenticación
                    if (!authenticateUser(requestCtx)) {
                        return;
                    }
                }
            }
        }
    }

    /**
     * Método auxiliar para autenticar usuarios.
     * Devuelve true si la autenticación es exitosa, false en caso contrario.
     */
    private boolean authenticateUser(ContainerRequestContext requestCtx) {
        List<String> headers = requestCtx.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (headers != null && !headers.isEmpty()) {
            String username;
            String password;
            try {
                String auth = headers.get(0);
                auth = auth.replace(AUTHORIZATION_HEADER_PREFIX, "");
                String decode = Base64.base64Decode(auth);
                StringTokenizer tokenizer = new StringTokenizer(decode, ":");
                username = tokenizer.nextToken();
                password = tokenizer.nextToken();
            } catch (Exception e) {
                requestCtx.abortWith(Response.status(Response.Status.BAD_REQUEST).build());
                return false;
            }

            try {
                Credentials credentials = em.createNamedQuery("Credentials.findUser", Credentials.class)
                    .setParameter("username", username)
                    .getSingleResult();

                if (credentials.getPassword().equals(password)) {
                    return true;
                } else {
                    requestCtx.abortWith(Response.status(Response.Status.FORBIDDEN).build());
                    return false;
                }
            } catch (NoResultException e) {
                requestCtx.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
                return false;
            }
        } else {
            requestCtx.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return false;
        }
    }
}
