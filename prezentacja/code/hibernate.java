@Entity
public class Event {
  private Long id;
  private String title;
  private Date date;
  @Id @GeneratedValue
  public Long getId() { return id; }
  private void setId(Long id) {this.id = id;}
  ...
}