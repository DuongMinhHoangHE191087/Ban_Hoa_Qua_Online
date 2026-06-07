package com.fruitmkt.service;

import com.fruitmkt.dao.SettlementDAO;
import com.fruitmkt.model.entity.ShopSettlement;

import java.sql.SQLException;
import java.util.List;

public class SettlementService {
    private final SettlementDAO settlementDAO = new SettlementDAO();
    private final com.fruitmkt.dao.SystemConfigDAO configDAO = new com.fruitmkt.dao.SystemConfigDAO();

    public int runAutoSettlement() throws SQLException {
        synchronized (SettlementService.class) {
            int freezeDays = configDAO.getInt(com.fruitmkt.config.AppConfig.CONFIG_FREEZE_DAYS, com.fruitmkt.config.AppConfig.FREEZE_DAYS_DEFAULT);
            return settlementDAO.runAutoSettlement(freezeDays);
        }
    }

    public List<ShopSettlement> getAllSettlements(String status, int page, int pageSize) throws SQLException {
        return settlementDAO.findAll(status, page, pageSize);
    }

    public List<ShopSettlement> getSettlementsByOwner(int ownerId) throws SQLException {
        return settlementDAO.findByOwner(ownerId);
    }

    public int countAllSettlements(String status) throws SQLException {
        return settlementDAO.countAll(status);
    }

    public void markPaid(int settlementId) throws SQLException {
        settlementDAO.markPaid(settlementId);
    }
}
