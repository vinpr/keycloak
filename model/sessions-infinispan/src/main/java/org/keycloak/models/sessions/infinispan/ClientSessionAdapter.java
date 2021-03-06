package org.keycloak.models.sessions.infinispan;

import org.infinispan.Cache;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.sessions.infinispan.entities.ClientSessionEntity;
import org.keycloak.models.sessions.infinispan.entities.SessionEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ClientSessionAdapter implements ClientSessionModel {

    private KeycloakSession session;
    private InfinispanUserSessionProvider provider;
    private Cache<String, SessionEntity> cache;
    private RealmModel realm;
    private ClientSessionEntity entity;

    public ClientSessionAdapter(KeycloakSession session, InfinispanUserSessionProvider provider, Cache<String, SessionEntity> cache, RealmModel realm, ClientSessionEntity entity) {
        this.session = session;
        this.provider = provider;
        this.cache = cache;
        this.realm = realm;
        this.entity = entity;
    }

    @Override
    public String getId() {
        return entity.getId();
    }

    @Override
    public RealmModel getRealm() {
        return realm;
    }

    @Override
    public ClientModel getClient() {
        return realm.getClientById(entity.getClient());
    }

    @Override
    public UserSessionModel getUserSession() {
        return entity.getUserSession() != null ? provider.getUserSession(realm, entity.getUserSession()) : null;
    }

    @Override
    public void setUserSession(UserSessionModel userSession) {
        if (userSession == null) {
            if (entity.getUserSession() != null) {
                provider.dettachSession(getUserSession(), this);
            }
            entity.setUserSession(null);
        } else {
            if (entity.getUserSession() != null) {
                if (entity.getUserSession().equals(userSession.getId())) {
                    return;
                } else {
                    provider.dettachSession(userSession, this);
                }
            } else {
                provider.attachSession(userSession, this);
            }

            entity.setUserSession(userSession.getId());
        }
        update();
    }

    @Override
    public String getRedirectUri() {
        return entity.getRedirectUri();
    }

    @Override
    public void setRedirectUri(String uri) {
        entity.setRedirectUri(uri);
        update();
    }

    @Override
    public int getTimestamp() {
        return entity.getTimestamp();
    }

    @Override
    public void setTimestamp(int timestamp) {
        entity.setTimestamp(timestamp);
        update();
    }

    @Override
    public String getAction() {
        return entity.getAction();
    }

    @Override
    public void setAction(String action) {
        entity.setAction(action);
        update();
    }

    @Override
    public Set<String> getRoles() {
        return entity.getRoles();
    }

    @Override
    public void setRoles(Set<String> roles) {
        entity.setRoles(roles);
        update();
    }

    @Override
    public Set<String> getProtocolMappers() {
        return entity.getProtocolMappers();
    }

    @Override
    public void setProtocolMappers(Set<String> protocolMappers) {
        entity.setProtocolMappers(protocolMappers);
        update();
    }

    @Override
    public String getAuthMethod() {
        return entity.getAuthMethod();
    }

    @Override
    public void setAuthMethod(String authMethod) {
        entity.setAuthMethod(authMethod);
        update();
    }

    @Override
    public String getNote(String name) {
        return entity.getNotes() != null ? entity.getNotes().get(name) : null;
    }

    @Override
    public void setNote(String name, String value) {
        if (entity.getNotes() == null) {
            entity.setNotes(new HashMap<String, String>());
        }
        entity.getNotes().put(name, value);
        update();
    }

    @Override
    public void removeNote(String name) {
        if (entity.getNotes() != null) {
            entity.getNotes().remove(name);
            update();
        }
    }

    @Override
    public void setUserSessionNote(String name, String value) {
        if (entity.getUserSessionNotes() == null) {
            entity.setUserSessionNotes(new HashMap<String, String>());
        }
        entity.getNotes().put(name, value);
        update();

    }

    @Override
    public Map<String, String> getUserSessionNotes() {
        if (entity.getUserSessionNotes() == null) {
            return Collections.EMPTY_MAP;
        }
        HashMap<String, String> copy = new HashMap<>();
        copy.putAll(entity.getUserSessionNotes());
        return copy;
    }

    void update() {
        provider.getTx().replace(cache, entity.getId(), entity);
    }
    @Override
    public Map<String, UserSessionModel.AuthenticatorStatus> getAuthenticators() {
        return entity.getAuthenticatorStatus();
    }

    @Override
    public void setAuthenticatorStatus(String authenticator, UserSessionModel.AuthenticatorStatus status) {
        entity.getAuthenticatorStatus().put(authenticator, status);

    }

    @Override
    public void setAuthenticatorStatus(Map<String, UserSessionModel.AuthenticatorStatus> status) {
        entity.setAuthenticatorStatus(status);
    }

    @Override
    public UserModel getAuthenticatedUser() {
        return session.users().getUserById(entity.getAuthUserId(), realm);    }

    @Override
    public void setAuthenticatedUser(UserModel user) {
        entity.setAuthUserId(user.getId());

    }

}
