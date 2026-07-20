# Transaction test run evidence — 2026-07-20

## Execution

- Database endpoint: local SQL Server `localhost:1433`, configured through existing `.env` keys.
- Main Java compilation: PASS.
- Selected transaction test compilation: PASS.
- JUnit: `35 tests`, `OK`, elapsed `14.866s`.
- Connection pool used the DriverManager fallback because Tomcat pool libraries were not on the selected test classpath.
- Test logs showed parameter redaction and generated-key/affected-row logging.

## Selected classes executed

| Test class | Covered flow |
|---|---|
| `CheckoutPaymentAuditTest` | CK order creation, payment transaction, valid/duplicate/wrong webhook paths |
| `PaymentWebhookRaceTest` | concurrent webhook deduplication |
| `DeliveryServiceTest` | delivery ownership/status/proof and service transitions |
| `ReturnServiceTest` | return request and decision rules |
| `ShopServiceTransactionRollbackTest` | rollback when shop profile persistence fails |
| `InventoryConcurrencyTest` | concurrent stock reservation invariant |
| `SettlementServiceTest` | settlement calculations/rules |
| `ReviewModerationValidationTest` | review validation/moderation |
| `ShopApprovalAPISecurityTest` | shop approval authorization/security |

## Evidence meaning

This run is stronger than screenshot evidence: logs show real database writes, reads, affected rows and cleanup deletes, while assertions verify outcomes. It supports the corresponding rows in `TRANSACTION_EVIDENCE_MAP.md` as executed test evidence.

It does not prove every Servlet/UI action or every production data scenario. The full `build-tools.ps1 test` command remains blocked before execution because the repository-wide test set currently contains missing Selenium dependencies and a compile error in `AdminOrderAndProductEditRegressionTest`; those are recorded separately and did not invalidate this selected JUnit run.

The test-only static helper issue in `AdminOrderAndProductEditRegressionTest` has since been corrected; the remaining full-suite blocker is the missing Selenium dependency.

## Cleanup

The selected tests use timestamped fixtures and `@After` cleanup. The JUnit run completed successfully and the log showed cleanup deletes for generated users/products/orders/payment data. A future CI gate should additionally run a database snapshot/count check before and after the suite.
