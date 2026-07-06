package com.example.wheelswap

import java.util.Locale

val pakistanMarketPrices = mapOf(
    // ══════════════════════════════
    // TOYOTA COROLLA — YEAR WISE
    // ══════════════════════════════
    "corolla xli 2010" to 2200000L, "corolla xli 2011" to 2400000L,
    "corolla xli 2012" to 2600000L, "corolla xli 2013" to 2800000L,
    "corolla xli 2014" to 3000000L, "corolla xli 2015" to 3200000L,
    "corolla xli 2016" to 3400000L, "corolla xli 2017" to 3600000L,
    "corolla xli 2018" to 3800000L, "corolla xli 2019" to 4000000L,
    "corolla xli 2020" to 4200000L, "corolla xli 2021" to 4500000L,
    "corolla xli 2022" to 5000000L, "corolla xli 2023" to 5500000L,

    "corolla gli 2010" to 2500000L, "corolla gli 2011" to 2700000L,
    "corolla gli 2012" to 2900000L, "corolla gli 2013" to 3100000L,
    "corolla gli 2014" to 3300000L, "corolla gli 2015" to 3600000L,
    "corolla gli 2016" to 3900000L, "corolla gli 2017" to 4200000L,
    "corolla gli 2018" to 4500000L, "corolla gli 2019" to 4800000L,
    "corolla gli 2020" to 5200000L, "corolla gli 2021" to 5600000L,
    "corolla gli 2022" to 6000000L, "corolla gli 2023" to 6500000L,

    "corolla altis 2010" to 3000000L, "corolla altis 2011" to 3300000L,
    "corolla altis 2012" to 3600000L, "corolla altis 2013" to 3900000L,
    "corolla altis 2014" to 4200000L, "corolla altis 2015" to 4600000L,
    "corolla altis 2016" to 5000000L, "corolla altis 2017" to 5400000L,
    "corolla altis 2018" to 5800000L, "corolla altis 2019" to 6200000L,
    "corolla altis 2020" to 6800000L, "corolla altis 2021" to 7500000L,
    "corolla altis 2022" to 8000000L, "corolla altis 2023" to 8500000L,

    "corolla grande 2015" to 5500000L, "corolla grande 2016" to 6000000L,
    "corolla grande 2017" to 6500000L, "corolla grande 2018" to 7000000L,
    "corolla grande 2019" to 7500000L, "corolla grande 2020" to 8000000L,
    "corolla grande 2021" to 8500000L, "corolla grande 2022" to 9000000L,

    // Generic Corolla fallback
    "toyota corolla" to 4500000L, "corolla" to 4500000L,

    // ══════════════════════════════
    // HONDA CIVIC — YEAR WISE
    // ══════════════════════════════
    "civic vti 2010" to 2800000L, "civic vti 2011" to 3000000L,
    "civic vti 2012" to 3200000L, "civic vti 2013" to 3500000L,
    "civic vti 2014" to 3800000L, "civic vti 2015" to 4200000L,
    "civic vti 2016" to 4600000L, "civic vti 2017" to 5000000L,
    "civic vti 2018" to 5500000L, "civic vti 2019" to 6000000L,
    "civic vti 2020" to 6500000L, "civic vti 2021" to 7000000L,

    "civic oriel 2010" to 3200000L, "civic oriel 2011" to 3500000L,
    "civic oriel 2012" to 3800000L, "civic oriel 2013" to 4200000L,
    "civic oriel 2014" to 4600000L, "civic oriel 2015" to 5000000L,
    "civic oriel 2016" to 5500000L, "civic oriel 2017" to 6000000L,
    "civic oriel 2018" to 6500000L, "civic oriel 2019" to 7000000L,
    "civic oriel 2020" to 7500000L, "civic oriel 2021" to 8000000L,
    "civic oriel 2022" to 8500000L, "civic oriel 2023" to 9000000L,

    "civic rs 2017" to 7000000L, "civic rs 2018" to 7500000L,
    "civic rs 2019" to 8000000L, "civic rs 2020" to 8500000L,
    "civic rs 2021" to 9000000L, "civic rs 2022" to 9500000L,
    "civic rs turbo 2017" to 7000000L, "civic rs turbo 2018" to 7500000L,
    "civic rs turbo 2019" to 8000000L, "civic rs turbo 2020" to 8500000L,
    "civic rs turbo 2021" to 9000000L, "civic rs turbo 2022" to 9500000L,

    "civic type r 2018" to 10000000L, "civic type r 2019" to 11000000L,
    "civic type r 2020" to 12000000L, "civic type r 2021" to 13000000L,

    // Generic Civic fallback
    "honda civic" to 6000000L, "civic" to 6000000L,

    // ══════════════════════════════
    // HONDA CITY — YEAR WISE
    // ══════════════════════════════
    "city aspire 2010" to 1800000L, "city aspire 2011" to 2000000L,
    "city aspire 2012" to 2300000L, "city aspire 2013" to 2600000L,
    "city aspire 2014" to 2900000L, "city aspire 2015" to 3200000L,
    "city aspire 2016" to 3500000L, "city aspire 2017" to 3800000L,
    "city aspire 2018" to 4200000L, "city aspire 2019" to 4600000L,
    "city aspire 2020" to 5000000L, "city aspire 2021" to 5500000L,
    "city aspire 2022" to 6000000L, "city aspire 2023" to 6500000L,

    "city prosmatec 2010" to 1600000L, "city prosmatec 2011" to 1800000L,
    "city prosmatec 2012" to 2000000L, "city prosmatec 2013" to 2200000L,
    "city prosmatec 2014" to 2500000L, "city prosmatec 2015" to 2800000L,
    "city prosmatec 2016" to 3100000L, "city prosmatec 2017" to 3400000L,
    "city prosmatec 2018" to 3700000L, "city prosmatec 2019" to 4000000L,
    "city prosmatec 2020" to 4400000L, "city prosmatec 2021" to 4800000L,

    "city vti 2010" to 1500000L, "city vti 2011" to 1700000L,
    "city vti 2012" to 1900000L, "city vti 2013" to 2100000L,
    "city vti 2014" to 2300000L, "city vti 2015" to 2600000L,

    // Generic City fallback
    "honda city" to 3500000L, "city" to 3500000L,

    // ══════════════════════════════
    // SUZUKI ALTO — YEAR WISE
    // ══════════════════════════════
    "alto vxr 2019" to 1600000L, "alto vxr 2020" to 1800000L,
    "alto vxr 2021" to 2000000L, "alto vxr 2022" to 2200000L,
    "alto vxr 2023" to 2400000L, "alto vxr 2024" to 2600000L,
    "alto vx 2019" to 1400000L, "alto vx 2020" to 1600000L,
    "alto vx 2021" to 1800000L, "alto vx 2022" to 2000000L,
    "alto vx 2023" to 2200000L, "alto vx 2024" to 2400000L,
    "alto ags 2020" to 1900000L, "alto ags 2021" to 2100000L,
    "alto ags 2022" to 2300000L, "alto ags 2023" to 2500000L,

    // Generic Alto fallback
    "suzuki alto" to 2200000L, "alto" to 2200000L,

    // ══════════════════════════════
    // SUZUKI CULTUS — YEAR WISE
    // ══════════════════════════════
    "cultus vxr 2017" to 1800000L, "cultus vxr 2018" to 2000000L,
    "cultus vxr 2019" to 2200000L, "cultus vxr 2020" to 2500000L,
    "cultus vxr 2021" to 2800000L, "cultus vxr 2022" to 3100000L,
    "cultus vxr 2023" to 3400000L, "cultus vxr 2024" to 3700000L,
    "cultus vxi 2017" to 1600000L, "cultus vxi 2018" to 1800000L,
    "cultus vxi 2019" to 2000000L, "cultus vxi 2020" to 2300000L,
    "cultus vxi 2021" to 2600000L, "cultus vxi 2022" to 2900000L,
    "cultus ags 2019" to 2400000L, "cultus ags 2020" to 2700000L,
    "cultus ags 2021" to 3000000L, "cultus ags 2022" to 3300000L,

    // Generic Cultus fallback
    "suzuki cultus" to 2800000L, "cultus" to 2800000L,

    // ══════════════════════════════
    // SUZUKI WAGON R — YEAR WISE
    // ══════════════════════════════
    "wagon r vxr 2014" to 1200000L, "wagon r vxr 2015" to 1400000L,
    "wagon r vxr 2016" to 1600000L, "wagon r vxr 2017" to 1800000L,
    "wagon r vxr 2018" to 2000000L, "wagon r vxr 2019" to 2200000L,
    "wagon r vxr 2020" to 2500000L, "wagon r vxr 2021" to 2800000L,
    "wagon r vxr 2022" to 3100000L, "wagon r vxr 2023" to 3400000L,
    "wagon r vxi 2014" to 1100000L, "wagon r vxi 2015" to 1300000L,
    "wagon r vxi 2016" to 1500000L, "wagon r vxi 2017" to 1700000L,
    "wagon r vxi 2018" to 1900000L, "wagon r vxi 2019" to 2100000L,
    "wagon r vxi 2020" to 2400000L, "wagon r vxi 2021" to 2700000L,
    "wagon r ags 2019" to 2300000L, "wagon r ags 2020" to 2600000L,
    "wagon r ags 2021" to 2900000L, "wagon r ags 2022" to 3200000L,

    // Generic Wagon R fallback
    "suzuki wagon r" to 2500000L, "wagon r" to 2500000L,

    // ══════════════════════════════
    // TOYOTA AQUA — YEAR WISE
    // ══════════════════════════════
    "toyota aqua 2012" to 2800000L, "toyota aqua 2013" to 3000000L,
    "toyota aqua 2014" to 3200000L, "toyota aqua 2015" to 3500000L,
    "toyota aqua 2016" to 3800000L, "toyota aqua 2017" to 4200000L,
    "toyota aqua 2018" to 4600000L, "toyota aqua 2019" to 5000000L,
    "toyota aqua 2020" to 5500000L, "toyota aqua 2021" to 6000000L,
    "toyota aqua 2022" to 6500000L, "toyota aqua 2023" to 7000000L,
    "aqua 2012" to 2800000L, "aqua 2013" to 3000000L,
    "aqua 2014" to 3200000L, "aqua 2015" to 3500000L,
    "aqua 2016" to 3800000L, "aqua 2017" to 4200000L,
    "aqua 2018" to 4600000L, "aqua 2019" to 5000000L,
    "aqua 2020" to 5500000L, "aqua 2021" to 6000000L,
    "aqua s 2014" to 3300000L, "aqua s 2015" to 3600000L,
    "aqua s 2016" to 3900000L, "aqua s 2017" to 4300000L,
    "aqua g 2014" to 3500000L, "aqua g 2015" to 3800000L,
    "aqua g 2016" to 4100000L, "aqua g 2017" to 4500000L,
    "aqua gr 2021" to 6500000L, "aqua gr 2022" to 7000000L,

    // Generic Aqua fallback
    "toyota aqua" to 4500000L, "aqua" to 4500000L,

    // ══════════════════════════════
    // TOYOTA PRIUS — YEAR WISE
    // ══════════════════════════════
    "toyota prius 2010" to 2500000L, "toyota prius 2011" to 2800000L,
    "toyota prius 2012" to 3200000L, "toyota prius 2013" to 3600000L,
    "toyota prius 2014" to 4000000L, "toyota prius 2015" to 4500000L,
    "toyota prius 2016" to 5000000L, "toyota prius 2017" to 5500000L,
    "toyota prius 2018" to 6000000L, "toyota prius 2019" to 6500000L,
    "toyota prius 2020" to 7000000L, "toyota prius 2021" to 7800000L,
    "toyota prius 2022" to 8500000L, "toyota prius 2023" to 9500000L,
    "prius 2010" to 2500000L, "prius 2011" to 2800000L,
    "prius 2012" to 3200000L, "prius 2013" to 3600000L,
    "prius 2014" to 4000000L, "prius 2015" to 4500000L,
    "prius 2016" to 5000000L, "prius 2017" to 5500000L,
    "prius 2018" to 6000000L, "prius 2019" to 6500000L,
    "prius 2020" to 7000000L, "prius 2021" to 7800000L,
    "prius s 2015" to 4800000L, "prius s 2016" to 5300000L,
    "prius s 2017" to 5800000L, "prius s 2018" to 6300000L,
    "prius phv 2017" to 6500000L, "prius phv 2018" to 7000000L,
    "prius phv 2019" to 7500000L, "prius phv 2020" to 8000000L,

    // Generic Prius fallback
    "toyota prius" to 5500000L, "prius" to 5500000L,

    // ══════════════════════════════
    // HONDA BR-V
    // ══════════════════════════════
    "honda brv 2017" to 3500000L, "honda brv 2018" to 3800000L,
    "honda brv 2019" to 4200000L, "honda brv 2020" to 4600000L,
    "honda brv 2021" to 5000000L, "honda brv 2022" to 5500000L,
    "brv 2017" to 3500000L, "brv 2018" to 3800000L,
    "brv 2019" to 4200000L, "brv 2020" to 4600000L,
    "honda brv" to 4500000L, "brv" to 4500000L,

    // ══════════════════════════════
    // KIA SPORTAGE — YEAR WISE
    // ══════════════════════════════
    "kia sportage 2020" to 6500000L, "kia sportage 2021" to 7500000L,
    "kia sportage 2022" to 8500000L, "kia sportage 2023" to 9500000L,
    "sportage alpha 2020" to 6000000L, "sportage alpha 2021" to 7000000L,
    "sportage alpha 2022" to 8000000L, "sportage alpha 2023" to 9000000L,
    "sportage awd 2021" to 8500000L, "sportage awd 2022" to 9500000L,
    "kia sportage" to 8000000L, "sportage" to 8000000L,

    // ══════════════════════════════
    // TOYOTA YARIS
    // ══════════════════════════════
    "yaris gli 2020" to 3800000L, "yaris gli 2021" to 4200000L,
    "yaris gli 2022" to 4600000L, "yaris gli 2023" to 5000000L,
    "yaris ativ 2020" to 3500000L, "yaris ativ 2021" to 3900000L,
    "yaris ativ 2022" to 4300000L, "yaris ativ 2023" to 4700000L,
    "toyota yaris" to 4000000L, "yaris" to 4000000L,

    // ══════════════════════════════
    // SUZUKI SWIFT
    // ══════════════════════════════
    "suzuki swift 2010" to 1500000L, "suzuki swift 2012" to 1800000L,
    "suzuki swift 2014" to 2200000L, "suzuki swift 2016" to 2600000L,
    "suzuki swift 2018" to 3000000L, "suzuki swift 2020" to 3500000L,
    "suzuki swift 2021" to 3800000L, "suzuki swift 2022" to 4200000L,
    "swift" to 3000000L,

    // ══════════════════════════════
    // TOYOTA VITZ
    // ══════════════════════════════
    "toyota vitz 2010" to 1800000L, "toyota vitz 2012" to 2000000L,
    "toyota vitz 2014" to 2300000L, "toyota vitz 2016" to 2700000L,
    "toyota vitz 2018" to 3100000L, "toyota vitz 2020" to 3500000L,
    "vitz 2010" to 1800000L, "vitz 2012" to 2000000L,
    "vitz 2014" to 2300000L, "vitz 2016" to 2700000L,
    "toyota vitz" to 2800000L, "vitz" to 2800000L,

    // ══════════════════════════════
    // DAIHATSU MIRA
    // ══════════════════════════════
    "daihatsu mira 2012" to 1800000L, "daihatsu mira 2014" to 2100000L,
    "daihatsu mira 2016" to 2400000L, "daihatsu mira 2018" to 2700000L,
    "daihatsu mira 2020" to 3000000L, "daihatsu mira 2021" to 3300000L,
    "mira 2012" to 1800000L, "mira 2014" to 2100000L,
    "mira 2016" to 2400000L, "mira 2018" to 2700000L,
    "daihatsu mira" to 2500000L, "mira" to 2500000L,

    // ══════════════════════════════
    // HYUNDAI TUCSON
    // ══════════════════════════════
    "tucson gls 2021" to 7000000L, "tucson gls 2022" to 8000000L,
    "tucson gls 2023" to 9000000L,
    "tucson ultimate 2021" to 8000000L, "tucson ultimate 2022" to 9000000L,
    "hyundai tucson" to 8000000L, "tucson" to 8000000L,

    // ══════════════════════════════
    // CHANGAN ALSVIN
    // ══════════════════════════════
    "changan alsvin 2021" to 3500000L, "changan alsvin 2022" to 3800000L,
    "changan alsvin 2023" to 4200000L,
    "alsvin" to 3800000L,

    // ══════════════════════════════
    // MG CARS
    // ══════════════════════════════
    "mg hs 2021" to 7500000L, "mg hs 2022" to 8000000L,
    "mg hs 2023" to 8500000L,
    "mg zs 2022" to 5500000L, "mg zs 2023" to 6000000L,
    "mg 5 2022" to 4500000L, "mg 5 2023" to 5000000L,
    "mg hs" to 8000000L, "mg zs" to 5800000L,

    // ══════════════════════════════
    // SUZUKI MEHRAN (classic)
    // ══════════════════════════════
    "suzuki mehran 2010" to 700000L, "suzuki mehran 2012" to 800000L,
    "suzuki mehran 2014" to 900000L, "suzuki mehran 2016" to 1000000L,
    "suzuki mehran 2018" to 1100000L, "suzuki mehran 2019" to 1200000L,
    "mehran" to 900000L,

    // ══════════════════════════════
    // HONDA CD70 — YEAR WISE
    // ══════════════════════════════
    "honda cd70 2015" to 80000L, "honda cd70 2016" to 85000L,
    "honda cd70 2017" to 90000L, "honda cd70 2018" to 95000L,
    "honda cd70 2019" to 100000L, "honda cd70 2020" to 110000L,
    "honda cd70 2021" to 120000L, "honda cd70 2022" to 130000L,
    "honda cd70 2023" to 145000L, "honda cd70 2024" to 160000L,
    "cd70 2018" to 95000L, "cd70 2019" to 100000L,
    "cd70 2020" to 110000L, "cd70 2021" to 120000L,
    "cd70 2022" to 130000L, "cd70 2023" to 145000L,
    "honda cd70" to 130000L, "cd70" to 130000L, "honda 70" to 130000L,

    // ══════════════════════════════
    // HONDA 125 — YEAR WISE
    // ══════════════════════════════
    "honda 125 2015" to 100000L, "honda 125 2016" to 105000L,
    "honda 125 2017" to 110000L, "honda 125 2018" to 118000L,
    "honda 125 2019" to 125000L, "honda 125 2020" to 135000L,
    "honda 125 2021" to 148000L, "honda 125 2022" to 160000L,
    "honda 125 2023" to 180000L, "honda 125 2024" to 200000L,
    "cg125 2018" to 118000L, "cg125 2019" to 125000L,
    "cg125 2020" to 135000L, "cg125 2021" to 148000L,
    "cg125 2022" to 160000L, "cg125 2023" to 180000L,
    "honda 125 self 2020" to 145000L, "honda 125 self 2021" to 158000L,
    "honda 125 self 2022" to 170000L, "honda 125 self 2023" to 190000L,
    "honda cg125" to 165000L, "honda 125" to 165000L, "cg125" to 165000L,

    // ══════════════════════════════
    // HONDA CB150F — YEAR WISE
    // ══════════════════════════════
    "honda cb150f 2016" to 160000L, "honda cb150f 2017" to 170000L,
    "honda cb150f 2018" to 180000L, "honda cb150f 2019" to 195000L,
    "honda cb150f 2020" to 215000L, "honda cb150f 2021" to 240000L,
    "honda cb150f 2022" to 270000L, "honda cb150f 2023" to 300000L,
    "honda cb150f 2024" to 330000L,
    "cb150f 2019" to 195000L, "cb150f 2020" to 215000L,
    "cb150f 2021" to 240000L, "cb150f 2022" to 270000L,
    "honda cb150f" to 280000L, "cb150f" to 280000L,

    // ══════════════════════════════
    // YAMAHA YBR — YEAR WISE
    // ══════════════════════════════
    "yamaha ybr 2015" to 100000L, "yamaha ybr 2016" to 110000L,
    "yamaha ybr 2017" to 120000L, "yamaha ybr 2018" to 130000L,
    "yamaha ybr 2019" to 145000L, "yamaha ybr 2020" to 160000L,
    "yamaha ybr 2021" to 180000L, "yamaha ybr 2022" to 210000L,
    "yamaha ybr 2023" to 240000L, "yamaha ybr 2024" to 270000L,
    "ybr 2018" to 130000L, "ybr 2019" to 145000L,
    "ybr 2020" to 160000L, "ybr 2021" to 180000L,
    "ybr 2022" to 210000L, "ybr 2023" to 240000L,
    "yamaha ybr125" to 200000L, "ybr125" to 200000L, "ybr" to 200000L,

    // ══════════════════════════════
    // SUZUKI GS150 — YEAR WISE
    // ══════════════════════════════
    "suzuki gs150 2016" to 140000L, "suzuki gs150 2017" to 150000L,
    "suzuki gs150 2018" to 165000L, "suzuki gs150 2019" to 180000L,
    "suzuki gs150 2020" to 200000L, "suzuki gs150 2021" to 225000L,
    "suzuki gs150 2022" to 255000L, "suzuki gs150 2023" to 290000L,
    "suzuki gs150 2024" to 320000L,
    "gs150 2019" to 180000L, "gs150 2020" to 200000L,
    "gs150 2021" to 225000L, "gs150 2022" to 255000L,
    "suzuki gs150" to 260000L, "gs150" to 260000L,

    // ══════════════════════════════
    // ELECTRIC BIKES — PAKISTAN
    // ══════════════════════════════
    "jolta je70" to 120000L, "jolta 70" to 120000L,
    "jolta je70d" to 120000L, "jolta electric 70" to 120000L,
    "jolta je100" to 220000L, "jolta 100" to 220000L,
    "jolta je125" to 249000L, "jolta 125" to 249000L,
    "jolta scooter" to 175000L, "jolta scooty" to 175000L,
    "jolta jes70" to 175000L,
    "jolta" to 150000L,

    "vlektra" to 280000L, "vlektra electric" to 280000L,
    "vlektra bolt" to 280000L, "vlektra storm" to 320000L,

    "united electric" to 180000L, "united e bike" to 180000L,
    "united e70" to 180000L,

    "road prince electric" to 160000L,
    "super power electric" to 155000L,
    "hi speed electric" to 150000L,

    "ev scooter" to 180000L, "electric scooter" to 180000L,
    "electric bike" to 160000L, "e bike" to 160000L
)

val modificationValues = mapOf(
    "alloy wheels" to 80000L, "alloys" to 80000L,
    "wrap" to 60000L, "vinyl wrap" to 60000L,
    "audio system" to 70000L, "sound system" to 70000L,
    "subwoofer" to 50000L, "tinted windows" to 25000L,
    "led lights" to 30000L, "hid lights" to 35000L,
    "turbo" to 150000L, "performance exhaust" to 45000L,
    "exhaust" to 40000L, "sunroof" to 120000L,
    "body kit" to 90000L, "android screen" to 55000L,
    "dashcam" to 15000L, "reverse camera" to 18000L
)

data class PricePrediction(
    val estimatedPrice: Long,
    val minPrice: Long,
    val maxPrice: Long,
    val depreciationApplied: Int,
    val mileageDeduction: Long,
    val conditionFactor: Double,
    val modificationsValue: Long,
    val verdict: String,
    val dealRating: String,
    val kmCategory: String
)

fun predictPrice(
    vehicleName: String,
    year: Int,
    mileageKm: Int,
    condition: String,
    selectedMods: List<String> = emptyList()
): PricePrediction? {

    val vehicleNameLower = vehicleName.lowercase().trim()
    
    // ── Step 1: Year ke saath exact match dhundo ──
    var basePrice = 0L
    val vehicleWithYear = "$vehicleNameLower $year"
    
    for ((key, price) in pakistanMarketPrices) {
        if (vehicleWithYear.contains(key) || key.contains(vehicleWithYear)) {
            basePrice = price
            break
        }
    }
    
    // ── Step 2: Agar year match nahi hua toh generic match ──
    if (basePrice == 0L) {
        for ((key, price) in pakistanMarketPrices) {
            if (vehicleNameLower.contains(key) && !key.contains(Regex("\\d{4}"))) {
                basePrice = price
                break
            }
        }
    }

    if (basePrice == 0L) return null

    // ── Step 3: Pakistani Market Factor ──
    // Pakistan mein cars depreciate nahi karti — inflation se price stable ya upar rehti hai
    val currentYear = 2024
    val age = currentYear - year

    val marketFactor = when {
        age <= 0 -> 1.0    // Brand new
        age <= 2 -> 0.90   // Slight loss
        age <= 4 -> 0.93   // Minor depreciation
        age <= 6 -> 0.96   // Almost stable
        age <= 8 -> 0.98   // Stable
        age <= 10 -> 1.00  // Same price
        age <= 12 -> 1.02  // Slight appreciation
        age <= 15 -> 1.05  // Appreciation
        else -> 1.03       // Old but stable
    }
    var estimatedPrice = (basePrice * marketFactor).toLong()

    // ── Step 4: KM Deduction ──
    val kmCategory: String
    val mileageDeduction: Long

    when {
        mileageKm == 0 -> {
            kmCategory = "🟢 Brand New / Not Driven"
            mileageDeduction = 0L
        }
        mileageKm < 10000 -> {
            kmCategory = "🟢 Almost New"
            mileageDeduction = -(basePrice * 0.03).toLong()
        }
        mileageKm < 30000 -> {
            kmCategory = "🟢 Low Mileage"
            mileageDeduction = 0L
        }
        mileageKm < 60000 -> {
            kmCategory = "🟡 Average Mileage"
            mileageDeduction = (basePrice * 0.05).toLong()
        }
        mileageKm < 100000 -> {
            kmCategory = "🟠 High Mileage"
            mileageDeduction = (basePrice * 0.10).toLong()
        }
        mileageKm < 150000 -> {
            kmCategory = "🔴 Very High Mileage"
            mileageDeduction = (basePrice * 0.18).toLong()
        }
        else -> {
            kmCategory = "🔴 Extremely High Mileage"
            mileageDeduction = (basePrice * 0.28).toLong()
        }
    }
    estimatedPrice -= mileageDeduction

    // ── Step 5: Condition Factor ──
    val conditionFactor = when (condition.lowercase()) {
        "showroom" -> 1.12
        "excellent" -> 1.05
        "good" -> 1.0
        "fair" -> 0.88
        "poor" -> 0.72
        else -> 1.0
    }
    estimatedPrice = (estimatedPrice * conditionFactor).toLong()

    // ── Step 6: Min/Max Range ──
    val minPrice = (estimatedPrice * 0.93).toLong()
    val maxPrice = (estimatedPrice * 1.07).toLong()

    // ── Step 7: Verdict ──
    val verdict = when {
        age <= 3 && conditionFactor >= 1.0 -> "✅ Excellent Deal"
        age <= 6 && conditionFactor >= 1.0 -> "✅ Good Deal"
        age <= 10 && conditionFactor >= 0.88 -> "⚠️ Fair Deal"
        conditionFactor < 0.88 -> "❌ Risky — Inspect First"
        else -> "⚠️ Consider Carefully"
    }

    // ── Step 8: Deal Rating ──
    val dealRating = when {
        age <= 2 -> "⭐⭐⭐⭐⭐"
        age <= 4 -> "⭐⭐⭐⭐"
        age <= 7 -> "⭐⭐⭐"
        age <= 10 -> "⭐⭐"
        else -> "⭐"
    }

    var modsValue = 0L
    for (mod in selectedMods) {
        val modLower = mod.lowercase(Locale.ROOT)
        for ((key, value) in modificationValues) {
            if (modLower.contains(key)) {
                modsValue += (value * 0.6).toLong()
                break
            }
        }
    }
    estimatedPrice += modsValue

    return PricePrediction(
        estimatedPrice = estimatedPrice,
        minPrice = minPrice,
        maxPrice = maxPrice,
        depreciationApplied = ((1 - marketFactor) * 100).toInt(),
        mileageDeduction = mileageDeduction,
        conditionFactor = conditionFactor,
        modificationsValue = modsValue,
        verdict = verdict,
        dealRating = dealRating,
        kmCategory = kmCategory
    )
}

fun formatPrice(amount: Long): String {
    return when {
        amount >= 10000000 -> "Rs. ${(amount / 10000000.0).format(1)} Crore"
        amount >= 100000 -> "Rs. ${(amount / 100000.0).format(1)} Lakh"
        else -> "Rs. $amount"
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)
