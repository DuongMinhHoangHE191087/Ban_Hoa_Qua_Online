package com.fruitmkt.tag;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * CurrencyFormatTag — Custom tag: {@code <ft:currency value=""/>}
 *
 * Output: "150.000 đ" (format tiền VND)
 * 
 * @author fruitmkt-team
 */
public class CurrencyFormatTag extends SimpleTagSupport {
    private BigDecimal value;

    @Override
    public void doTag() throws JspException, IOException {
        if (value == null) {
            getJspContext().getOut().write("N/A");
            return;
        }
        // TODO: Tuỳ chỉnh format nếu cần
        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        fmt.setMinimumFractionDigits(0);
        getJspContext().getOut().write(fmt.format(value) + " đ");
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
