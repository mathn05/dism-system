package com.csdlpt.web.service;

import com.csdlpt.web.entity.AppUser;
import com.csdlpt.web.entity.Customer;
import com.csdlpt.web.entity.ImportEntity;
import com.csdlpt.web.entity.Product;
import com.csdlpt.web.entity.Sale;
import com.csdlpt.web.entity.Station;
import com.csdlpt.web.entity.Supplier;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IdGenerationService {

    private final EntityManager entityManager;

    public IdGenerationService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public String nextUserId(String stationId) {
        return nextNumberedId(AppUser.class, "U_" + stationCode(stationId));
    }

    public String nextImportId(String stationId) {
        return nextNumberedId(ImportEntity.class, "I_" + stationCode(stationId));
    }

    public String nextSaleId(String stationId) {
        return nextNumberedId(Sale.class, "S_" + stationCode(stationId));
    }

    public String nextProductId() {
        return nextNumberedId(Product.class, "P");
    }

    public String nextSupplierId() {
        return nextNumberedId(Supplier.class, "SUP");
    }

    public String nextCustomerId() {
        return nextNumberedId(Customer.class, "CUS");
    }

    public String normalizeManualId(String id) {
        return id == null ? null : id.trim().toUpperCase(Locale.ROOT);
    }

    public String stationId(Station station) {
        if (Boolean.TRUE.equals(station.getHeadquarter())) {
            return "HQ";
        }

        String base = firstNonBlank(station.getName(), station.getAddress());
        if (base == null) {
            throw new IllegalArgumentException("Station name or address is required to generate ID");
        }
        String id = "CN_" + initials(base);
        if (exists(Station.class, id)) {
            throw new IllegalArgumentException("Station ID already exists: " + id);
        }
        return id;
    }

    private String nextNumberedId(Class<?> entityClass, String prefix) {
        String normalizedPrefix = prefix.toUpperCase(Locale.ROOT);
        Pattern pattern = Pattern.compile("^" + Pattern.quote(normalizedPrefix) + "_(\\d{3,})$");
        int max = 0;

        for (String id : loadIds(entityClass)) {
            if (id == null) {
                continue;
            }
            Matcher matcher = pattern.matcher(id.toUpperCase(Locale.ROOT));
            if (matcher.matches()) {
                max = Math.max(max, Integer.parseInt(matcher.group(1)));
            }
        }

        return "%s_%03d".formatted(normalizedPrefix, max + 1);
    }

    private List<String> loadIds(Class<?> entityClass) {
        String entityName = entityManager.getMetamodel().entity(entityClass).getName();
        return entityManager.createQuery("select e.id from " + entityName + " e", String.class)
            .getResultList();
    }

    private boolean exists(Class<?> entityClass, String id) {
        String entityName = entityManager.getMetamodel().entity(entityClass).getName();
        return !entityManager.createQuery("select e.id from " + entityName + " e where e.id = :id", String.class)
            .setParameter("id", id)
            .setMaxResults(1)
            .getResultList()
            .isEmpty();
    }

    private String stationCode(String stationId) {
        String normalized = normalizeManualId(stationId);
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException("Station ID is required");
        }
        if ("HQ".equals(normalized)) {
            return "HQ";
        }
        if (normalized.startsWith("CN_") && normalized.length() > 3) {
            return normalized.substring(3);
        }
        return normalized;
    }

    private String initials(String value) {
        String ascii = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replace('Đ', 'D')
            .replace('đ', 'd');

        StringBuilder result = new StringBuilder();
        for (String part : ascii.trim().split("[^A-Za-z0-9]+")) {
            if (!part.isBlank()) {
                result.append(Character.toUpperCase(part.charAt(0)));
            }
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException("Cannot generate station code from station name or address");
        }
        return result.toString();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
