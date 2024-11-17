package authn;
import java.io.Serializable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import model.entities.Customer;

@Entity
//@NamedQuery(name="Credentials.findUser", 
            //query="SELECT c FROM Credentials c WHERE c.username = :username")
@XmlRootElement
public class Credentials implements Serializable { 
    @Id
    @SequenceGenerator(name="Credentials_Gen", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Credentials_Gen") 
    private Long id;
    @OneToOne
    private Customer customer;
    /*@Column(unique=true)
    @NotNull(message="Username can't be null")
    private String username;*/
    @NotNull(message="Password can't be null")
    private String password;
    
    public Credentials(){
        
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    

    /*public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }*/

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
