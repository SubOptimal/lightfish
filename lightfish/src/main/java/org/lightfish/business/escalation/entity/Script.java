package org.lightfish.business.escalation.entity;

import java.util.Objects;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author adam bien, adam-bien.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Entity
@NamedQueries({
    @NamedQuery(name=Script.findAll,query="Select s from Script s"),
    @NamedQuery(name=Script.findAllActive,query="Select s from Script s where s.active = true")
})
public class Script {
    public static final String PREFIX = "org.lightfish.business.scripting.entity.";
    public static final String findAll = PREFIX + "findAll";
    public static final String findAllActive = PREFIX + "findAllActive";
    
    @Id
    private String name;
    @Lob
    private String script;
    
    private boolean active;
    
    public Script() {
    }

    public Script(String name, String script, boolean active) {
        this.name = name;
        this.script = script;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return script;
    }

    public void setContent(String script) {
        this.script = script;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + Objects.hashCode(this.script);
        hash = 97 * hash + (this.active ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Script other = (Script) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.script, other.script)) {
            return false;
        }
        if (this.active != other.active) {
            return false;
        }
        return true;
    }

    
}