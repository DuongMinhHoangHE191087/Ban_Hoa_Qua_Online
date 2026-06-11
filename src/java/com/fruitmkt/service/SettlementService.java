package com.fruitmkt.service;

import com.fruitmkt.config.AppConfig;
import com.fruitmkt.dao.SettlementDAO;
import com.fruitmkt.dao.SystemConfigDAO;
import com.fruitmkt.model.entity.ShopSettlement;

import java.sql.SQLException;
import java.util.List;

public class SettlementService {
    private final SettlementDAO settlementDAO = new SettlementDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();

    public int runAutoSettlement() throws SQLException {
        synchronized (SettlementService.class) {
            int freezeDays = configDAO.getInt(AppConfig.CONFIG_FREEZE_DAYS, AppConfig.FREEZE_DAYS_DEFAULT);
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
