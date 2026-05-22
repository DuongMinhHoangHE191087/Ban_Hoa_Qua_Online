# Ban_hoa_qua_online

## Project Context
- Java 17 web app using Jakarta Servlet 6, JSP, JSTL, Tomcat 10, Ant, and NetBeans.
- Architecture is layered: servlet/controller -> service -> DAO -> database.
- UI pages live under `web/WEB-INF/jsp`; shared fragments live under `web/WEB-INF/jsp/common`.

## Working Rules
- Inspect nearby code before editing and mirror the local pattern.
- Keep changes small and surgical.
- Keep SQL in DAO classes only.
- Use `PreparedStatement`, try-with-resources, and parameterized queries.
- Preserve PRG after POST and keep JSPs behind `WEB-INF`.
- Validate user input before persistence or redirecting.
- Do not edit generated NetBeans build files unless the failure clearly requires it.

## Domain Rules
- One order belongs to one shop owner.
- Guest cart state should remain session/local-storage based.
- Delivery staff is a separate actor from customer and shop owner.
- Returns, refunds, and settlement are part of the domain and should stay consistent with the schema and SRS.

## Project References
- Check `docs/SRS_Section_3_Functional_Requirements_FruitShop.docx.md` for functional rules.
- Check `database/Schema.sql` and `database/Setup_OnlineFruitShopping.sql` before changing persistence logic.
- Use nearby DAO, service, servlet, filter, tag, and JSP examples as the source of truth.

## Validation
- Prefer the smallest useful validation after edits.
- If a change touches SQL, verify it against the schema and the nearest DAO/service usage.
- If a change touches JSP or static assets, confirm the path and include flow still match the project structure.