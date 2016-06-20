package es.unizar.iaaa.geofencing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import java.sql.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import es.unizar.iaaa.geofencing.view.View;

@Entity
@Table(name = "NOTIFICATIONS")
public class Notification {

    private Long id;
    private Rule rule;
    private User user;
    private String status;
    private Date date;

    public Notification() {
    }

    public Notification(@JsonProperty("id") Long id, @JsonProperty("rule") Rule rule,
                        @JsonProperty("user") User user, @JsonProperty("status") String status,
                        @JsonProperty("date") Date date) {
        this.id = id;
        this.rule = rule;
        this.user = user;
        this.status = status;
        this.date = date;
    }

    @Id
    @GeneratedValue
    @Column(name = "ID", unique = true, nullable = false)
    @JsonView(View.NotificationBaseView.class)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonView(View.NotificationBaseView.class)
    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonView(View.NotificationCompleteView.class)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Column(name = "STATUS", nullable = false, length = 15)
    @JsonView(View.NotificationBaseView.class)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Column(name = "DATE", nullable = false, length = 15)
    @JsonView(View.NotificationCompleteView.class)
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String toString() {
        return "Notification(id: " + id + " rule id: " + rule.getId() + " user id: " + user.getId() + " status: " + status +
                " date: " + date + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification notification = (Notification) o;
        return id == notification.id &&
                Objects.equals(rule, notification.rule) &&
                Objects.equals(user, notification.user) &&
                Objects.equals(status, notification.status) &&
                Objects.equals(date, notification.date);
    }
}