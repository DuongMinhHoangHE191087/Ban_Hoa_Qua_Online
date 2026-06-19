package service.shop;

import config.AppConfig;
import dao.shop.SettlementDAO;
import dao.system.SystemConfigDAO;
import model.entity.shop.ShopSettlement;

import java.sql.SQLException;
import java.util.List;

public class SettlementService {

    private final SettlementDAO settlementDAO = new SettlementDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();

    public int runAutoSettlement() throws SQLException {
        synchronized (SettlementService.class) {
            // PAY-03: use hours for freeze window so sub-day release is possible.
            // AppConfig.FREEZE_HOURS_DEFAULT = 12 will be added by another agent;
            // fall back to literal 12 if the constant is not yet present. // TODO use AppConfig.FREEZE_HOURS_DEFAULT
            int freezeHours = configDAO.getInt("settlement_freeze_hours", 12);
            return settlementDAO.runAutoSettlementByHours(freezeHours);
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
