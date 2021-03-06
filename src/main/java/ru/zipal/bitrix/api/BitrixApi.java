package ru.zipal.bitrix.api;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.zipal.bitrix.api.model.*;
import ru.zipal.bitrix.api.model.enums.EntityType;
import ru.zipal.bitrix.api.model.enums.OwnerType;
import ru.zipal.bitrix.api.serialize.Serializer;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class BitrixApi<User, Activity, Contact extends HasId, Lead extends HasId, Company extends HasId, Deal extends HasId, UserEnumField extends HasId> {
    private final BitrixClient client;
    private final Serializer serializer;
    private final String domain;

    private final Class<User> userClass;
    private final Class<Activity> activityClass;
    private final Class<Contact> contactClass;
    private final Class<Lead> leadClass;
    private final Class<Company> companyClass;
    private final Class<Deal> dealClass;
    private final Class<UserEnumField> userEnumFieldClass;

    protected BitrixApi(BitrixClient client, Serializer serializer, String domain, Class<User> userClass, Class<Activity> activityClass, Class<Contact> contactClass, Class<Lead> leadClass, Class<Company> companyClass, Class<Deal> dealClass, Class<UserEnumField> userEnumFieldClass) {
        this.client = client;
        this.serializer = serializer;
        this.domain = domain;
        this.contactClass = contactClass;
        this.activityClass = activityClass;
        this.leadClass = leadClass;
        this.userClass = userClass;
        this.companyClass = companyClass;
        this.dealClass = dealClass;
        this.userEnumFieldClass = userEnumFieldClass;
    }

    public static BitrixApi<BitrixUser, BitrixActivity, BitrixContact, BitrixLead, BitrixCompany, BitrixDeal, BitrixUserEnumField> createDefault(BitrixClient client, Serializer serializer, String domain) {
        return new BitrixApi<>(client, serializer, domain, BitrixUser.class, BitrixActivity.class, BitrixContact.class, BitrixLead.class, BitrixCompany.class, BitrixDeal.class, BitrixUserEnumField.class);
    }

    public static <User, Activity, Contact extends HasId, Lead extends HasId, Company extends HasId, Deal extends HasId, UserEnumField extends HasId> BitrixApi<User, Activity, Contact, Lead, Company, Deal, UserEnumField> custom(BitrixClient client, Serializer serializer, String domain, Class<User> userClass, Class<Activity> activityClass, Class<Contact> contactClass, Class<Lead> leadClass, Class<Company> companyClass, Class<Deal> dealClass, Class<UserEnumField> userEnumFieldClass) {
        return new BitrixApi<>(client, serializer, domain, userClass, activityClass, contactClass, leadClass, companyClass, dealClass, userEnumFieldClass);
    }

    public BitrixPage<Contact> listContacts(Integer start, NameValuePair... additional) throws BitrixApiException {
        return list("crm.contact.list", contactClass, start, additional);
    }

    public BitrixPage<Contact> listContacts(Integer start, JSONObject params) throws BitrixApiException {
        return list("crm.contact.list", contactClass, start, params);
    }

    public Map<Long, Contact> getContacts(Collection<Long> ids) throws BitrixApiException {
        return getBatch(contactClass, ids, "crm.contact.get");
    }

    public Map<Long, Lead> getLeads(Collection<Long> ids) throws BitrixApiException {
        return getBatch(leadClass, ids, "crm.lead.get");
    }

    public Map<Long, Company> getCompanies(Collection<Long> ids) throws BitrixApiException {
        return getBatch(companyClass, ids, "crm.company.get");
    }

    protected <T extends HasId> Map<Long, T> getBatch(Class<T> clazz, Collection<Long> ids, String method) throws BitrixApiException {
        final JSONObject json = client.execute(domain, "batch", ids.stream().map(id -> new BasicNameValuePair("cmd[e_" + id + "]", method + "?ID=" + id)).collect(Collectors.toList()));
        final JSONObject result = json.getJSONObject("result").getJSONObject("result");
        final HashMap<Long, T> map = new HashMap<>();
        for (Long id : ids) {
            if (result.has("e_" + id)) {
                map.put(id, serializer.deserialize(clazz, result.getJSONObject("e_" + id)));
            }
        }
        return map;
    }

    protected Class<?> getEntityClass(EntityType entityType) {
        switch (entityType) {
            case USER:
                return userClass;
            case ACTIVITY:
                return activityClass;
            case CONTACT:
                return contactClass;
            case LEAD:
                return leadClass;
            case COMPANY:
                return companyClass;
        }
        throw new IllegalArgumentException("not supported: " + entityType);
    }

    protected Map<Pair<EntityType, Long>, Object> getBatch(Collection<Pair<EntityType, Long>> whatToLoad) throws BitrixApiException {
        final JSONObject json = client.execute(domain, "batch", whatToLoad.stream().map(pair -> new BasicNameValuePair("cmd[e_" + pair.getValue() + "]", pair.getKey().getGetMethod() + "?ID=" + pair.getValue())).collect(Collectors.toList()));
        final JSONObject result = json.getJSONObject("result").getJSONObject("result");
        final HashMap<Pair<EntityType, Long>, Object> map = new HashMap<>();
        for (Pair<EntityType, Long> pair : whatToLoad) {
            if (result.has("e_" + pair.getValue())) {
                map.put(pair, serializer.deserialize(getEntityClass(pair.getKey()), result.getJSONObject("e_" + pair.getValue())));
            }
        }
        return map;
    }

    public Contact getContact(long id) throws BitrixApiException {
        return serializer.deserialize(contactClass, client.execute(domain, "crm.contact.get", Collections.singletonList(new BasicNameValuePair("id", Long.toString(id)))).getJSONObject("result"));
    }

    public Lead getLead(long id) throws BitrixApiException {
        return serializer.deserialize(leadClass, client.execute(domain, "crm.lead.get", Collections.singletonList(new BasicNameValuePair("id", Long.toString(id)))).getJSONObject("result"));
    }

    public Company getCompany(long id) throws BitrixApiException {
        return serializer.deserialize(companyClass, client.execute(domain, "crm.company.get", Collections.singletonList(new BasicNameValuePair("id", Long.toString(id)))).getJSONObject("result"));
    }

    public BitrixPage<Company> listCompanies(Integer start, NameValuePair... additional) throws BitrixApiException {
        return list("crm.company.list", companyClass, start, additional);
    }

    public Long createCompany(Company company) throws BitrixApiException {
        return client.execute(domain, "crm.company.add", serializer.serialize(company)).getLong("result");
    }

    public void removeCompany(long id) throws BitrixApiException {
        client.execute(domain, "crm.company.delete", Collections.singletonList(new BasicNameValuePair("id", Long.toString(id))));
    }

    public void removeContact(long id) throws BitrixApiException {
        client.execute(domain, "crm.contact.delete", Collections.singletonList(new BasicNameValuePair("id", Long.toString(id))));
    }

    public Long createContact(Contact contact) throws BitrixApiException {
        return client.execute(domain, "crm.contact.add", serializer.serialize(contact)).getLong("result");
    }

    public void updateContact(Contact contact) throws BitrixApiException {
        final List<NameValuePair> params = serializer.serialize(contact);
        params.add(new BasicNameValuePair("id", Long.toString(contact.getId())));
        client.execute(domain, "crm.contact.update", params);
    }

    public void updateCompany(Company company) throws BitrixApiException {
        final List<NameValuePair> params = serializer.serialize(company);
        params.add(new BasicNameValuePair("id", Long.toString(company.getId())));
        client.execute(domain, "crm.company.update", params);
    }

    public BitrixPage<Lead> listLeads(Integer start, NameValuePair... additional) throws BitrixApiException {
        return list("crm.lead.list", leadClass, start, additional);
    }

    public Long createLead(Lead lead) throws BitrixApiException {
        return client.execute(domain, "crm.lead.add", serializer.serialize(lead)).getLong("result");
    }

    public void updateLead(Lead lead) throws BitrixApiException {
        final List<NameValuePair> params = serializer.serialize(lead);
        params.add(new BasicNameValuePair("id", Long.toString(lead.getId())));
        client.execute(domain, "crm.lead.update", params);
    }

    public BitrixPage<User> listUsers(Integer start) throws BitrixApiException {
        final List<NameValuePair> params = new ArrayList<>();
        if (start != null) {
            params.add(new BasicNameValuePair("start", start.toString()));
        }
        return getPage(client.execute(domain, "user.get", params), userClass);
    }

    public BitrixPage<Activity> listActivities(Integer start, NameValuePair... additional) throws BitrixApiException {
        return list("crm.activity.list", activityClass, start, additional);
    }

    public Activity getActivity(long id) throws BitrixApiException {
        return serializer.deserialize(activityClass, client.execute(domain, "crm.activity.get", Collections.singletonList(new BasicNameValuePair("id", Long.toString(id)))).getJSONObject("result"));
    }

    public Long createActivity(Activity activity, NameValuePair... additional) throws BitrixApiException {
        final List<NameValuePair> params = serializer.serialize(activity);
        if (additional != null) {
            params.addAll(Arrays.asList(additional));
        }
        return client.execute(domain, "crm.activity.add", params).getLong("result");
    }

    public Deal getDeal(long id) throws BitrixApiException {
        return serializer.deserialize(dealClass, client.execute(domain, "crm.deal.get", Collections.singletonList(new BasicNameValuePair("id", Long.toString(id)))).getJSONObject("result"));
    }

    public Long createDeal(Deal deal) throws BitrixApiException {
        return client.execute(domain, "crm.deal.add", serializer.serialize(deal)).getLong("result");
    }

    public void updateDeal(Deal deal) throws BitrixApiException {
        final List<NameValuePair> params = serializer.serialize(deal);
        params.add(new BasicNameValuePair("id", Long.toString(deal.getId())));
        client.execute(domain, "crm.deal.update", params);
    }

    public UserEnumField getUserEnumField(long id, OwnerType ownerType) throws BitrixApiException {

        return serializer.deserialize(userEnumFieldClass, client.execute(domain, String.format("crm.%s.userfield.get", ownerType.getUrlPath()), Collections.singletonList(new BasicNameValuePair("id", Long.toString(id)))).getJSONObject("result"));
    }

    public void updateUserEnumField(UserEnumField userField, OwnerType ownerType) throws BitrixApiException {
        final List<NameValuePair> params = serializer.serialize(userField);
        params.add(new BasicNameValuePair("id", Long.toString(userField.getId())));
        client.execute(domain, String.format("crm.%s.userfield.update", ownerType.getUrlPath()), params);
    }

    public void bindEvent(String event, String handler) throws BitrixApiException {
        client.execute(domain, "event.bind", Arrays.asList(new BasicNameValuePair("event", event), new BasicNameValuePair("handler", handler)));
    }

    public void unbindEvent(String event, String handler) throws BitrixApiException {
        client.execute(domain, "event.unbind", Arrays.asList(new BasicNameValuePair("event", event), new BasicNameValuePair("handler", handler)));
    }

    public boolean isAdmin(String domain) throws BitrixApiException {
        return client.execute(domain, "user.admin", Collections.emptyList()).getBoolean("result");
    }

    private <T> BitrixPage<T> list(String method, Class<T> entityClass, Integer start, NameValuePair... additional) throws BitrixApiException {
        final List<NameValuePair> params = new ArrayList<>();
        if (start != null) {
            params.add(new BasicNameValuePair("start", start.toString()));
        }
        if (additional != null) {
            params.addAll(Arrays.asList(additional));
        }
        return getPage(client.execute(domain, method, params), entityClass);
    }

    private <T> BitrixPage<T> list(String method, Class<T> entityClass, Integer start, JSONObject params) throws BitrixApiException {
        if (start != null) {
            params.put("start", start.toString());
        }
        return getPage(client.execute(domain, method, params), entityClass);
    }

    private <T> BitrixPage<T> getPage(JSONObject json, Class<T> clazz) throws BitrixApiException {
        final JSONArray array = json.getJSONArray("result");
        return new BitrixPage<>(json.has("next") ? json.getInt("next") : null, serializer.deserializeArray(clazz, array));
    }
}
