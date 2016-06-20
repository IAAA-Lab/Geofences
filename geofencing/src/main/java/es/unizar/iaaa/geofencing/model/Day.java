package es.unizar.iaaa.geofencing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import java.sql.Time;
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
@Table(name = "DAYS")
public class Day {

    private Long id;
    private String day;
    private Time opening_time;
    private Time closing_time;
    private Boolean whole_day;
    private Rule rule;

    public Day() {
    }

    public Day(@JsonProperty("id") Long id, @JsonProperty("day") String day, @JsonProperty("opening_time") Time opening_time,
               @JsonProperty("closing_time") Time closing_time, @JsonProperty("whole_day") Boolean whole_day,
               @JsonProperty("rule") Rule rule) {
        this.id = id;
        this.day = day;
        this.opening_time = opening_time;
        this.closing_time = closing_time;
        this.whole_day = whole_day;
        this.rule = rule;
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

    @Column(name = "DAY", nullable = false, length = 9)
    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    @Column(name = "OPENING_TIME", nullable = false, length = 15)
    public Time getOpeningTime() {
        return opening_time;
    }

    public void setOpeningTime(Time opening_time) {
        this.opening_time = opening_time;
    }

    @Column(name = "CLOSING_TIME", nullable = false, length = 15)
    public Time getClosingTime() {
        return closing_time;
    }

    public void setClosingTime(Time closing_time) {
        this.closing_time = closing_time;
    }

    @Column(name = "WHOLE_DAY", nullable = false, length = 15)
    public Boolean getWholeDay() {
        return whole_day;
    }

    public void setWholeDay(Boolean whole_day) {
        this.whole_day = whole_day;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public String toString() {
        return "Day(id: " + id + " day: " + day + " opening_time: " + opening_time + " closing_time: " + closing_time + " whole_day: " + whole_day +
                " rule: " + rule + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Day day = (Day) o;
        return id == day.id &&
                Objects.equals(day, day.day) &&
                Objects.equals(opening_time, day.opening_time) &&
                Objects.equals(closing_time, day.closing_time) &&
                Objects.equals(whole_day, day.whole_day) &&
                Objects.equals(rule, day.rule);
    }
}
