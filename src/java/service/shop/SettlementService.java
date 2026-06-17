package service.shop;

import config.AppConfig;
import dao.shop.SettlementDAO;
import dao.system.SystemConfigDAO;
import model.entity.shop.ShopSettlement;

import java.sql.SQLException;
import java.util.List;

import java.util.logging.Logger;
import util.LoggerUtil;

public class SettlementService {

    private static final Logger log = LoggerUtil.getLogger(SettlementService.class);

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

    public ShopSettlement getSettlementById(int settlementId) throws SQLException {
        return settlementDAO.findById(settlementId);
    }

    public List<model.entity.shop.ShopSettlementOrder> getOrdersBySettlementId(int settlementId) throws SQLException {
        return settlementDAO.findOrdersBySettlementId(settlementId);
    }
}
