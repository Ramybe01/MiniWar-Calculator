#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Roblox MiniWar Pazar Ekonomisi Hesaplayıcı
Modern CustomTkinter Masaüstü Arayüzü

Gereksinimler:
    pip install customtkinter
"""

import customtkinter as ctk
from decimal import Decimal, InvalidOperation
import tkinter.messagebox as messagebox

# --- Veri Seti ---
miniwar_items = {
    "Buğday": 14, "Mısır": 18, "Kömür": 27, "Ahşap": 38, 
    "Un": 49, "Havuçlar": 67, "Kitaplar": 94, "Demir": 120, 
    "Yağ": 120, "Beton": 170, "Altın": 260, "Para Çantası": 410, 
    "Araştırma": 560, "Elmaslar": 840, "Uran Cevheri": 1330, 
    "Sabit Uran": 2100, "Robo Kafası": 2500, "Veri Küpü": 3000, 
    "Karanlık Madde": 5000, "Uzaylı Özü": 20000, "Anti madde": 40000, 
    "Kuantum Çekirdeği": 90000, "Süpernova Şarjı": 225000, 
    "Gamma Işını": 450000, "Anomali Çekirdeği": 675000
}


def format_turkish_number(val: Decimal) -> str:
    """Büyük sayıları Türk formatında binlik ayraçlı (noktalı) gösterir."""
    val_int = int(val)
    # Binlikleri nokta ile ayır
    return f"{val_int:,}".replace(",", ".")


def get_abbreviation(val: Decimal) -> str:
    """Katrilyon, trilyon, milyar gibi büyük sayılar için okunabilir ek bilgi döndürür."""
    v = float(val)
    if v >= 1e21:
        return f"≈ {v/1e21:.2f} Seksilyon"
    elif v >= 1e18:
        return f"≈ {v/1e18:.2f} Kentilyon"
    elif v >= 1e15:
        return f"≈ {v/1e15:.2f} Katrilyon"
    elif v >= 1e12:
        return f"≈ {v/1e12:.2f} Trilyon"
    elif v >= 1e9:
        return f"≈ {v/1e9:.2f} Milyar"
    elif v >= 1e6:
        return f"≈ {v/1e6:.2f} Milyon"
    elif v >= 1e3:
        return f"≈ {v/1e3:.2f} Bin"
    return ""


class MiniWarCalculatorApp(ctk.CTk):
    def __init__(self):
        super().__init__()

        # Tema ve Pencere Yapılandırması
        ctk.set_appearance_mode("dark")
        ctk.set_default_color_theme("blue")

        self.title("Roblox MiniWar - Pazar Ekonomisi Hesaplayıcı")
        self.geometry("540x720")
        self.resizable(False, False)

        self.calculated_value_raw = ""

        self._build_ui()

    def _build_ui(self):
        # Üst Başlık Kartı
        header_frame = ctk.CTkFrame(self, corner_radius=12, fg_color="#1a1829")
        header_frame.pack(fill="x", padx=20, pady=(20, 10))

        title_label = ctk.CTkLabel(
            header_frame,
            text="MINIWAR EKONOMİ HESAPLAYICI",
            font=ctk.CTkFont(size=20, weight="bold"),
            text_color="#FFD166"
        )
        title_label.pack(pady=(15, 2))

        subtitle_label = ctk.CTkLabel(
            header_frame,
            text="Roblox MiniWar Pazar & Etkinlik Kâr Hesaplama Aracı",
            font=ctk.CTkFont(size=12),
            text_color="#A7A9BE"
        )
        subtitle_label.pack(pady=(0, 15))

        # Ana Giriş Formu Kartı
        form_frame = ctk.CTkFrame(self, corner_radius=12, fg_color="#1e1c2e")
        form_frame.pack(fill="x", padx=20, pady=10)

        # 1. Eşya Seçimi
        item_label = ctk.CTkLabel(
            form_frame,
            text="Satılacak Eşya:",
            font=ctk.CTkFont(size=14, weight="bold")
        )
        item_label.pack(anchor="w", padx=20, pady=(15, 4))

        item_names = list(miniwar_items.keys())
        self.item_combo = ctk.CTkComboBox(
            form_frame,
            values=item_names,
            command=self._on_item_selected,
            font=ctk.CTkFont(size=14),
            dropdown_font=ctk.CTkFont(size=13),
            height=38
        )
        self.item_combo.set(item_names[0])
        self.item_combo.pack(fill="x", padx=20, pady=(0, 6))

        self.base_price_label = ctk.CTkLabel(
            form_frame,
            text=f"Temel Birim Fiyatı: {miniwar_items[item_names[0]]:,} Coins".replace(",", "."),
            font=ctk.CTkFont(size=12),
            text_color="#06D6A0"
        )
        self.base_price_label.pack(anchor="w", padx=20, pady=(0, 12))

        # 2. Adet Girişi
        qty_label = ctk.CTkLabel(
            form_frame,
            text="Satılacak Toplam Adet (Miktar):",
            font=ctk.CTkFont(size=14, weight="bold")
        )
        qty_label.pack(anchor="w", padx=20, pady=(4, 4))

        self.qty_entry = ctk.CTkEntry(
            form_frame,
            placeholder_text="Örn: 1000, 50000, 1000000",
            font=ctk.CTkFont(size=14),
            height=38
        )
        self.qty_entry.insert(0, "1000")
        self.qty_entry.pack(fill="x", padx=20, pady=(0, 8))

        # Hızlı miktar butonları
        qty_chips_frame = ctk.CTkFrame(form_frame, fg_color="transparent")
        qty_chips_frame.pack(fill="x", padx=20, pady=(0, 14))

        for text, val in [("+1K", 1000), ("+10K", 10000), ("+100K", 100000), ("+1M", 1000000)]:
            btn = ctk.CTkButton(
                qty_chips_frame,
                text=text,
                width=65,
                height=26,
                fg_color="#2b2644",
                hover_color="#3e3763",
                command=lambda v=val: self._add_qty(v)
            )
            btn.pack(side="left", padx=(0, 6))

        # 3. Kâr Oranı Girişi
        profit_label = ctk.CTkLabel(
            form_frame,
            text="% Kâr Oranı (Etkinlik Dalgalanması):",
            font=ctk.CTkFont(size=14, weight="bold")
        )
        profit_label.pack(anchor="w", padx=20, pady=(4, 4))

        self.profit_entry = ctk.CTkEntry(
            form_frame,
            placeholder_text="Varsayılan: 340",
            font=ctk.CTkFont(size=14),
            height=38
        )
        self.profit_entry.insert(0, "340")  # Varsayılan 340
        self.profit_entry.pack(fill="x", padx=20, pady=(0, 15))

        # Hesapla Butonu
        calc_button = ctk.CTkButton(
            self,
            text="⚡ TOPLAM KAZANCI HESAPLA ⚡",
            font=ctk.CTkFont(size=15, weight="bold"),
            height=46,
            fg_color="#8A4FFF",
            hover_color="#7036E0",
            corner_radius=10,
            command=self.calculate
        )
        calc_button.pack(fill="x", padx=20, pady=(10, 10))

        # Sonuç Kartı
        self.result_frame = ctk.CTkFrame(self, corner_radius=12, fg_color="#181628", border_width=2, border_color="#FFD166")
        self.result_frame.pack(fill="both", expand=True, padx=20, pady=(0, 20))

        res_title = ctk.CTkLabel(
            self.result_frame,
            text="TOPLAM TAHMİNİ KAZANÇ",
            font=ctk.CTkFont(size=13, weight="bold"),
            text_color="#A7A9BE"
        )
        res_title.pack(pady=(16, 4))

        self.result_value_label = ctk.CTkLabel(
            self.result_frame,
            text="---",
            font=ctk.CTkFont(size=26, weight="bold"),
            text_color="#FFD166",
            wraplength=480
        )
        self.result_value_label.pack(pady=(4, 2))

        self.abbrev_label = ctk.CTkLabel(
            self.result_frame,
            text="",
            font=ctk.CTkFont(size=14, weight="bold"),
            text_color="#06D6A0"
        )
        self.abbrev_label.pack(pady=(0, 8))

        self.detail_label = ctk.CTkLabel(
            self.result_frame,
            text="Hesaplama yapmak için butona basınız.",
            font=ctk.CTkFont(size=12),
            text_color="#CCCCCC"
        )
        self.detail_label.pack(pady=(0, 10))

        # Sonucu Kopyala Butonu
        self.copy_button = ctk.CTkButton(
            self.result_frame,
            text="📋 Sonucu Panoya Kopyala",
            width=200,
            height=32,
            fg_color="#2b2644",
            hover_color="#3e3763",
            command=self._copy_to_clipboard
        )
        self.copy_button.pack(pady=(0, 14))

        # İlk açılışta varsayılan hesaplamayı çalıştır
        self.calculate()

    def _on_item_selected(self, choice):
        base_price = miniwar_items.get(choice, 0)
        self.base_price_label.configure(
            text=f"Temel Birim Fiyatı: {base_price:,} Coins".replace(",", ".")
        )

    def _add_qty(self, val: int):
        try:
            current = int(self.qty_entry.get().replace(".", "").replace(",", "").strip())
        except ValueError:
            current = 0
        new_val = current + val
        self.qty_entry.delete(0, "end")
        self.qty_entry.insert(0, str(new_val))

    def calculate(self):
        item_name = self.item_combo.get()
        if item_name not in miniwar_items:
            messagebox.showerror("Hata", "Lütfen geçerli bir eşya seçiniz.")
            return

        base_price = Decimal(miniwar_items[item_name])

        # Adet Kontrolü
        qty_str = self.qty_entry.get().replace(".", "").replace(",", "").strip()
        try:
            qty = Decimal(qty_str)
            if qty < 0:
                raise ValueError
        except (InvalidOperation, ValueError):
            messagebox.showerror("Geçersiz Girdi", "Lütfen geçerli bir adet (miktar) sayısı giriniz.")
            return

        # Kâr Oranı Kontrolü
        profit_str = self.profit_entry.get().replace("%", "").replace(",", ".").strip()
        try:
            profit_rate = Decimal(profit_str)
        except (InvalidOperation, ValueError):
            messagebox.showerror("Geçersiz Girdi", "Lütfen geçerli bir % Kâr Oranı giriniz.")
            return

        # Matematiksel Formül:
        # Toplam Kazanç = Adet * (Temel Fiyat * (1 + (Kâr Oranı / 100)))
        profit_multiplier = Decimal(1) + (profit_rate / Decimal(100))
        unit_final_price = base_price * profit_multiplier
        total_earnings = qty * unit_final_price

        # Formatlama (Büyük sayılar için binlik nokta ayracı)
        formatted_total = format_turkish_number(total_earnings)
        abbrev = get_abbreviation(total_earnings)

        self.calculated_value_raw = str(int(total_earnings))
        self.result_value_label.configure(text=f"{formatted_total} Coins")
        self.abbrev_label.configure(text=abbrev)

        unit_str = f"{unit_final_price:,.2f}".replace(",", "X").replace(".", ",").replace("X", ".")
        self.detail_label.configure(
            text=f"Eşya: {item_name} | Birim Satış: {unit_str} Coins\n"
                 f"Formül: {qty} * ({base_price} * (1 + {profit_rate}%))"
        )

    def _copy_to_clipboard(self):
        if not self.calculated_value_raw:
            return
        self.clipboard_clear()
        self.clipboard_append(self.calculated_value_raw)
        self.update()
        messagebox.showinfo("Kopyalandı", f"{self.calculated_value_raw} panoya kopyalandı!")


if __name__ == "__main__":
    app = MiniWarCalculatorApp()
    app.mainloop()
