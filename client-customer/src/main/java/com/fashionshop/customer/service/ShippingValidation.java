package com.fashionshop.customer.service;

public final class ShippingValidation {
    private ShippingValidation() {}
    public static String normalizePhone(String phone) {
        if (phone == null) throw new IllegalArgumentException("Vui lòng nhập số điện thoại.");
        String value = phone.trim().replaceAll("[\\s().-]", "");
        if (value.startsWith("+84")) value = "0" + value.substring(3);
        if (!value.matches("0[35789][0-9]{8}"))
            throw new IllegalArgumentException("Số điện thoại di động Việt Nam phải gồm 10 số, bắt đầu bằng 03/05/07/08/09.");
        return value;
    }
    public static void validate(String name, String phone, String address, String note) {
        check(name, 2, 100, "Tên người nhận");
        normalizePhone(phone);
        check(address, 10, 500, "Địa chỉ nhận hàng");
        if (note != null && note.trim().length() > 1000)
            throw new IllegalArgumentException("Ghi chú tối đa 1000 ký tự.");
    }
    private static void check(String value, int min, int max, String label) {
        if (value == null || value.trim().length() < min || value.trim().length() > max)
            throw new IllegalArgumentException(label + " phải có từ " + min + " đến " + max + " ký tự.");
        if (value.chars().anyMatch(ch -> Character.isISOControl(ch) && ch != '\n' && ch != '\r' && ch != '\t'))
            throw new IllegalArgumentException(label + " chứa ký tự không hợp lệ.");
    }
}
