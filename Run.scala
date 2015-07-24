package geomapfish

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

import java.lang.Math


class Run extends Simulation {
    val random = new util.Random

    val host = "mapfish-geoportal.demo-camptocamp.com"
    val cache_version = random.nextInt()
    val defaults_feeder = Array(Map(
        "theme" -> "Transport",
        "group" -> "Transport",
        "lang" -> "fr",
        "cache_version" -> cache_version
    )).random
    val theme = "Transport"
    val group = theme
    val lang = "fr"

    val nbUser = Integer.getInteger("nbuser").intValue
    val nbInstance = Integer.getInteger("nbinstance").intValue

    val spaceTime = 2
    val basicTime = 40.0
//    val nbTimes = Math.ceil(nbUser * spaceTime / basicTime).toInt * 10
    val nbTimes = Integer.getInteger("nbtimes", 20).intValue

    System.out.println("Get config:")
    System.out.println(s"nbUser: $nbUser")
    System.out.println(s"nbInstance: $nbInstance")
    System.out.println(s"nbTimes: nbTimes")

    val rampTime = nbUser * spaceTime
    val fts_choises = ('a' to 'z') ++ ('0' to '9')

    val extent = Array(512691, 149736, 551491, 171836)
    def randX: Int = extent.get(0) + random.nextInt(extent.get(2) - extent.get(0))
    def randY: Int = extent.get(1) + random.nextInt(extent.get(3) - extent.get(1))
    def getBbox(width: Int, height: Int)(x: Int, y: Int, resolution: Double): Map[String, Any] = {
        val x2 = x + width * resolution
        val y2 = y + height * resolution
        Map(
            "BBOX" -> "%d,%d,%f,%f".format(x, y, x2, y2),
            "WIDTH" -> width,
            "HEIGHT" -> height
        )
    }

    val instance_ids = (0 to nbInstance - 1).map { n => "%d".format(n) }
    val resolutions = Array(0.05, 0.1, 0.25, 0.5, 1, 2, 5, 10, 20, 50, 100, 200)
    val layers = Map(
        "0" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "dn_dissolution_gypse", "dn_glissement_surf", "dn_glissement_prof", "at_camac_en_cours", "at_camac_anciennes", "mo_label", "ep_regulation_pression", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "at_lim_constr", "en_cit", "en_cit_gest", "mo_ddp", "mo_baths", "mo_batss", "mo_label_automatique", "mo_cs", "ad_entree_rcb", "en_reserve_faune", "en_imns_s", "en_imns_l", "ep_vanne_parcelle", "ep_vanne_batiment", "ep_conduite_zone_pression", "ep_vanne_raccords"),
        "1" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "dn_glissement_prof", "dn_dissolution_gypse", "dn_glissement_surf", "at_camac_en_cours", "at_camac_anciennes", "at_lim_constr", "mo_cs", "mo_label", "tp_bateau_s", "tp_bus_l", "tp_bus_s", "tp_train_s", "tp_train_l", "en_cit", "en_cit_gest", "mo_label_automatique", "en_reserve_faune", "en_imns_s", "en_imns_l", "ad_entree_rcb", "mo_baths", "at_lisforet", "at_limconstrforet"),
        "2" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "eu_ch", "eu_coll", "ep_conduite", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "at_pga_ext", "at_ra_surf", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "mo_baths", "mo_cs", "mo_par", "mo_agr", "mo_label", "mo_label_auto", "at_camac_anciennes", "at_camac_en_cours", "at_lim_constr", "ep_regulation_pression", "bt_zonedanger_r", "bt_zonedanger_j", "bt_zonedanger_b", "bt_zonebatir", "bt_parcelles_bat", "bt_parcelles_non_bat"),
        "3" -> Array("at_pga", "at_dsb", "mo_batss", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "mo_agr", "ep_cond", "eu_evac_par", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_ch_label", "at_pga_ppa", "mo_baths", "at_camac_en_cours", "at_camac_anciennes", "ep_regulation_pression", "at_lim_constr", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "at_surf_assolement", "at_ra_pt", "at_ra_surf", "en_cit", "en_cit_gest", "mo_label_automatique", "mo_cs", "mo_label", "en_imns_s", "dn_dissolution_gypse", "dn_glissement_prof", "dn_glissement_surf", "at_zone_proteaux", "at_sct_proteaux", "at_lisforet", "ad_entree_rcb", "en_reserve_faune", "en_courbes_niveau", "en_imns_l", "ep_vanne_batiment", "ep_vanne_parcelle", "ep_conduite_zone_pression", "ep_vanne_raccords", "pc_surf", "pc_parc", "pc_dp_comm", "pc_ddp", "at_limconstrforet", "gz_conduite", "gz_vanne", "gz_elemontage", "tc_conduite", "tc_elemontage", "cd_ouvrage", "cd_bat_racc", "cd_cond"),
        "4" -> Array("at_pga", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "cad_batss", "cad_odl", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par_label", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_evac_par", "at_ra_pt", "at_ra_surf", "at_surf_assolement", "cad_baths", "at_pga_ppa", "at_dsb", "at_pga_ext", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "at_camac_anciennes", "at_camac_en_cours", "at_lim_constr", "ep_regulation_pression", "mo_label", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "mo_label_automatique", "mo_cs", "en_cit", "en_cit_gest", "en_reserve_faune", "en_imns_s", "en_imns_l", "ad_entree_rcb", "mo_baths", "at_lisforet", "at_limconstrforet", "en_courbes_niveau"),
        "5" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "dn_glissement_prof", "dn_dissolution_gypse", "dn_glissement_surf", "at_camac_en_cours", "at_camac_anciennes", "at_lim_constr", "mo_cs", "mo_label", "tp_bateau_s", "tp_bus_l", "tp_bus_s", "tp_train_l", "tp_train_s", "en_cit", "en_cit_gest", "mo_label_automatique", "en_imns_l", "en_imns_s", "en_reserve_faune", "ad_entree_rcb", "mo_baths"),
        "6" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "mo_agr", "ep_cond", "at_ra_pt", "at_ra_surf", "eu_evac_par", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_ch_label", "at_pga_ppa", "at_surf_assolement", "ep_hydrante", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "at_camac_en_cours", "at_camac_anciennes", "ep_regulation_pression", "at_lim_constr", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "mo_label_automatique", "en_cit", "en_cit_gest", "mo_label", "mo_cs", "mo_baths", "ad_entree_rcb", "en_reserve_faune", "en_imns_s", "en_imns_l"),
        "7" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "dn_dissolution_gypse", "dn_glissement_surf", "dn_glissement_prof", "at_camac_en_cours", "at_camac_anciennes", "mo_label", "ep_regulation_pression", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "at_lim_constr", "en_cit", "en_cit_gest", "mo_ddp", "mo_baths", "mo_batss", "mo_label_automatique", "mo_cs", "ad_entree_rcb", "en_reserve_faune", "en_imns_s", "en_imns_l"),
        "8" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "at_lim_constr", "at_camac_en_cours", "at_camac_anciennes", "ep_regulation_pression", "mo_label", "mo_baths", "mo_ddp", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "en_cit", "en_cit_gest", "mo_batss", "mo_label_automatique", "mo_cs", "ad_entree_rcb", "en_reserve_faune", "en_imns_s", "en_imns_l"),
        "9" -> Array("at_pga", "at_dsb", "mo_batss", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "mo_agr", "ep_cond", "eu_evac_par", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_ch_label", "at_pga_ppa", "mo_baths", "at_camac_en_cours", "at_camac_anciennes", "ep_regulation_pression", "at_lim_constr", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "at_surf_assolement", "at_ra_pt", "at_ra_surf", "en_cit", "en_cit_gest", "mo_label_automatique", "mo_cs", "mo_label", "en_imns_s", "dn_dissolution_gypse", "dn_glissement_prof", "dn_glissement_surf", "at_zone_proteaux", "at_sct_proteaux", "ad_entree_rcb", "en_reserve_faune", "en_courbes_niveau", "en_imns_l", "ep_vanne_batiment", "ep_vanne_parcelle", "ep_conduite_zone_pression", "ep_vanne_raccords", "pc_surf", "pc_parc", "pc_dp_comm", "pc_ddp", "at_lisforet", "at_limconstrforet", "gz_conduite", "gz_vanne", "gz_elemontage", "tc_conduite", "tc_elemontage", "cd_ouvrage", "cd_bat_racc", "cd_cond"),
        "10" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_baths", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "at_camac_anciennes", "at_camac_en_cours", "ep_regulation_pression", "at_lim_constr", "at_class_route", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "en_cit", "en_cit_gest", "en_reserve_faune", "en_imns_l", "en_imns_s", "mo_label_automatique", "ad_entree_rcb", "mo_label", "at_lisforet", "at_limconstrforet", "en_courbes_niveau"),
        "11" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_baths", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "dn_glissement_prof", "dn_dissolution_gypse", "dn_glissement_surf", "at_lim_constr", "ep_regulation_pression", "en_cit", "en_cit_gest", "en_reserve_faune", "en_imns_s", "en_imns_l", "mo_label_automatique", "ad_entree_rcb", "mo_label", "en_courbes_niveau", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "pc_surf", "pc_parc", "pc_dp_comm", "pc_ddp"),
        "12" -> Array("at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_baths", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_surf_assolement", "at_pga_ppa", "at_pga", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "tp_train_s", "tp_bus_s", "tp_bateau_s", "tp_bus_l", "tp_train_l", "at_camac_anciennes", "at_camac_en_cours", "ep_regulation_pression", "at_lim_constr", "mo_label", "mo_cs", "tx_zone_public", "tx_zone_prive", "en_cit", "en_cit_gest", "en_reserve_faune", "en_imns_l", "en_imns_s", "mo_label_automatique", "ad_entree_rcb", "en_courbes_niveau", "at_lisforet", "at_limconstrforet", "coll_precis_pl", "eu_coll_sans_precis_pl"),
        "13" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_baths", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "at_camac_anciennes", "at_camac_en_cours", "at_lim_constr", "ep_regulation_pression", "mo_label", "mo_cs", "tp_bateau_s", "tp_bus_s", "tp_train_s", "tp_train_l", "tp_bus_l", "mo_label_automatique", "en_cit", "en_cit_gest", "en_reserve_faune", "en_imns_l", "en_imns_s", "ad_entree_rcb", "at_lisforet", "at_limconstrforet", "en_courbes_niveau"),
        "14" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "at_camac_en_cours", "at_camac_anciennes", "at_lim_constr", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "en_cit", "en_cit_gest", "mo_label", "mo_cs", "mo_label_automatique", "en_imns_l", "en_imns_s", "en_reserve_faune", "ad_entree_rcb", "mo_baths"),
        "15" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "at_lim_constr", "at_camac_en_cours", "at_camac_anciennes", "ep_regulation_pression", "mo_label", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "mo_baths", "mo_ddp", "en_cit", "en_cit_gest", "mo_batss", "mo_label_automatique", "mo_cs", "ad_entree_rcb", "en_reserve_faune", "en_imns_l", "en_imns_s"),
        "16" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "at_pga_ppa", "at_surf_assolement", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "at_camac_en_cours", "at_camac_anciennes", "at_lim_constr", "mo_cs", "mo_label", "tp_bateau_s", "tp_bus_l", "tp_bus_s", "tp_train_s", "tp_train_s", "en_cit", "en_cit_gest", "at_ra_pt", "at_ra_surf", "el_armoire", "el_cable", "el_luminaire", "el_tube", "eu_ch_label", "eu_evac_par", "mo_label_automatique", "en_imns_l", "en_imns_s", "en_reserve_faune", "mo_baths", "ad_entree_rcb"),
        "17" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_cond", "at_ra_pt", "at_ra_surf", "eu_evac_par", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_ch_label", "at_pga_ppa", "at_surf_assolement", "ep_hydrante", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "ep_regulation_pression", "at_camac_en_cours", "at_camac_anciennes", "at_lim_constr", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "mo_label", "mo_cs", "mo_baths", "mo_agr", "pc_ddp", "pc_parc", "pc_surf", "pc_dp_comm", "mo_label_automatique", "en_cit", "en_cit_gest", "en_reserve_faune", "en_imns_s", "en_imns_l"),
        "18" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "at_camac_en_cours", "at_camac_anciennes", "ep_regulation_pression", "at_lim_constr", "mo_label", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "en_cit", "en_cit_gest", "mo_ddp", "mo_baths", "mo_batss", "mo_label_automatique", "mo_cs", "ad_entree_rcb", "en_reserve_faune", "en_imns_s", "en_imns_l"),
        "19" -> Array("at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "at_pga", "dn_glissement_prof", "dn_glissement_prof", "dn_dissolution_gypse", "at_camac_en_cours", "at_camac_anciennes", "at_lim_constr", "mo_cs", "mo_label", "tp_bateau_s", "tp_bus_l", "tp_bus_s", "tp_train_s", "tp_train_l", "en_cit", "en_cit_gest", "mo_label_automatique", "en_imns_l", "en_imns_s", "en_reserve_faune", "mo_baths", "ad_entree_rcb"),
        "20" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_baths", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "at_camac_anciennes", "at_camac_en_cours", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "en_cit_gest", "mo_label", "mo_cs", "at_lim_constr", "eu_ch", "en_cit", "en_reserve_faune", "en_imns_l", "en_imns_s", "mo_label_automatique", "ad_entree_rcb", "at_lisforet", "at_limconstrforet"),
        "21" -> Array("at_pga", "at_dsb", "mo_batss", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "mo_agr", "ep_cond", "eu_evac_par", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_ch_label", "at_pga_ppa", "mo_baths", "at_camac_en_cours", "at_camac_anciennes", "ep_regulation_pression", "at_lim_constr", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "at_surf_assolement", "at_ra_pt", "at_ra_surf", "en_cit", "en_cit_gest", "mo_label_automatique", "mo_cs", "mo_label", "en_imns_s", "dn_dissolution_gypse", "dn_glissement_prof", "dn_glissement_surf", "at_zone_proteaux", "at_sct_proteaux", "at_lisforet", "ad_entree_rcb", "en_reserve_faune", "en_courbes_niveau", "en_imns_l", "ep_vanne_batiment", "ep_vanne_parcelle", "ep_conduite_zone_pression", "ep_vanne_raccords", "pc_surf", "pc_parc", "pc_dp_comm", "pc_ddp", "at_limconstrforet", "gz_conduite", "gz_vanne", "gz_elemontage", "tc_conduite", "tc_elemontage", "cd_ouvrage", "cd_bat_racc", "cd_cond"),
        "22" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "dn_glissement_prof", "dn_dissolution_gypse", "dn_glissement_surf", "at_camac_en_cours", "at_camac_anciennes", "at_lim_constr", "mo_cs", "mo_label", "tp_bateau_s", "tp_bus_l", "tp_bus_s", "tp_train_l", "tp_train_s", "en_cit", "en_cit_gest", "mo_label_automatique", "tmp_ptkm", "tmp_station", "tmp_axe_tr", "tmp_pn_p", "tmp_constr_l", "en_imns_l", "en_imns_s", "en_reserve_faune", "mo_baths", "ad_entree_rcb"),
        "23" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "dn_dissolution_gypse", "dn_glissement_surf", "dn_glissement_prof", "at_camac_en_cours", "at_camac_anciennes", "mo_label", "ep_regulation_pression", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "at_lim_constr", "en_cit", "en_cit_gest", "mo_ddp", "mo_baths", "mo_batss", "mo_label_automatique", "mo_cs", "ad_entree_rcb", "en_reserve_faune", "en_imns_s", "en_imns_l"),
        "24" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "at_pga_ext", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "at_camac_en_cours", "at_camac_anciennes", "ep_regulation_pression", "at_lim_constr", "mo_label", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "mo_baths", "mo_ddp", "en_cit", "en_cit_gest", "mo_batss", "mo_label_automatique", "mo_cs", "ad_entree_rcb", "en_reserve_faune", "en_imns_s", "en_imns_l"),
        "25" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "at_camac_en_cours", "at_camac_anciennes", "ep_regulation_pression", "mo_label", "at_lim_constr", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "mo_baths", "mo_ddp", "en_cit", "en_cit_gest", "mo_batss", "mo_label_automatique", "mo_cs", "ad_entree_rcb", "en_reserve_faune", "en_imns_s", "en_imns_l"),
        "26" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "dn_dissolution_gypse", "dn_glissement_surf", "dn_glissement_prof", "at_camac_en_cours", "at_camac_anciennes", "mo_label", "ep_regulation_pression", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "at_lim_constr", "en_cit", "en_cit_gest", "mo_ddp", "mo_baths", "mo_batss", "mo_label_automatique", "mo_cs", "ad_entree_rcb", "en_reserve_faune", "en_imns_s", "en_imns_l", "ep_vanne_parcelle", "ep_vanne_batiment", "ep_conduite_zone_pression", "ep_vanne_raccords"),
        "27" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "dn_glissement_prof", "dn_dissolution_gypse", "dn_glissement_surf", "at_camac_en_cours", "at_camac_anciennes", "at_lim_constr", "mo_cs", "mo_label", "tp_bateau_s", "tp_bus_l", "tp_bus_s", "tp_train_s", "tp_train_l", "en_cit", "en_cit_gest", "mo_label_automatique", "en_reserve_faune", "en_imns_s", "en_imns_l", "ad_entree_rcb", "mo_baths", "at_lisforet", "at_limconstrforet"),
        "28" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "eu_ch", "eu_coll", "ep_conduite", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "at_pga_ext", "at_ra_surf", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "mo_baths", "mo_cs", "mo_par", "mo_agr", "mo_label", "mo_label_auto", "at_camac_anciennes", "at_camac_en_cours", "at_lim_constr", "ep_regulation_pression", "bt_zonedanger_r", "bt_zonedanger_j", "bt_zonedanger_b", "bt_zonebatir", "bt_parcelles_bat", "bt_parcelles_non_bat"),
        "29" -> Array("at_pga", "at_dsb", "mo_batss", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "mo_agr", "ep_cond", "eu_evac_par", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_ch_label", "at_pga_ppa", "mo_baths", "at_camac_en_cours", "at_camac_anciennes", "ep_regulation_pression", "at_lim_constr", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "at_surf_assolement", "at_ra_pt", "at_ra_surf", "en_cit", "en_cit_gest", "mo_label_automatique", "mo_cs", "mo_label", "en_imns_s", "dn_dissolution_gypse", "dn_glissement_prof", "dn_glissement_surf", "at_zone_proteaux", "at_sct_proteaux", "at_lisforet", "ad_entree_rcb", "en_reserve_faune", "en_courbes_niveau", "en_imns_l", "ep_vanne_batiment", "ep_vanne_parcelle", "ep_conduite_zone_pression", "ep_vanne_raccords", "pc_surf", "pc_parc", "pc_dp_comm", "pc_ddp", "at_limconstrforet", "gz_conduite", "gz_vanne", "gz_elemontage", "tc_conduite", "tc_elemontage", "cd_ouvrage", "cd_bat_racc", "cd_cond"),
        "30" -> Array("at_pga", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "cad_batss", "cad_odl", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par_label", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_evac_par", "at_ra_pt", "at_ra_surf", "at_surf_assolement", "cad_baths", "at_pga_ppa", "at_dsb", "at_pga_ext", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "at_camac_anciennes", "at_camac_en_cours", "at_lim_constr", "ep_regulation_pression", "mo_label", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "mo_label_automatique", "mo_cs", "en_cit", "en_cit_gest", "en_reserve_faune", "en_imns_s", "en_imns_l", "ad_entree_rcb", "mo_baths", "at_lisforet", "at_limconstrforet", "en_courbes_niveau"),
        "31" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "dn_glissement_prof", "dn_dissolution_gypse", "dn_glissement_surf", "at_camac_en_cours", "at_camac_anciennes", "at_lim_constr", "mo_cs", "mo_label", "tp_bateau_s", "tp_bus_l", "tp_bus_s", "tp_train_l", "tp_train_s", "en_cit", "en_cit_gest", "mo_label_automatique", "en_imns_l", "en_imns_s", "en_reserve_faune", "ad_entree_rcb", "mo_baths"),
        "32" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "mo_agr", "ep_cond", "at_ra_pt", "at_ra_surf", "eu_evac_par", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_ch_label", "at_pga_ppa", "at_surf_assolement", "ep_hydrante", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "at_camac_en_cours", "at_camac_anciennes", "ep_regulation_pression", "at_lim_constr", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "mo_label_automatique", "en_cit", "en_cit_gest", "mo_label", "mo_cs", "mo_baths", "ad_entree_rcb", "en_reserve_faune", "en_imns_s", "en_imns_l"),
        "33" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "dn_dissolution_gypse", "dn_glissement_surf", "dn_glissement_prof", "at_camac_en_cours", "at_camac_anciennes", "mo_label", "ep_regulation_pression", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "at_lim_constr", "en_cit", "en_cit_gest", "mo_ddp", "mo_baths", "mo_batss", "mo_label_automatique", "mo_cs", "ad_entree_rcb", "en_reserve_faune", "en_imns_s", "en_imns_l"),
        "34" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "at_lim_constr", "at_camac_en_cours", "at_camac_anciennes", "ep_regulation_pression", "mo_label", "mo_baths", "mo_ddp", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "en_cit", "en_cit_gest", "mo_batss", "mo_label_automatique", "mo_cs", "ad_entree_rcb", "en_reserve_faune", "en_imns_s", "en_imns_l"),
        "35" -> Array("at_pga", "at_dsb", "mo_batss", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "mo_agr", "ep_cond", "eu_evac_par", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_ch_label", "at_pga_ppa", "mo_baths", "at_camac_en_cours", "at_camac_anciennes", "ep_regulation_pression", "at_lim_constr", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "at_surf_assolement", "at_ra_pt", "at_ra_surf", "en_cit", "en_cit_gest", "mo_label_automatique", "mo_cs", "mo_label", "en_imns_s", "dn_dissolution_gypse", "dn_glissement_prof", "dn_glissement_surf", "at_zone_proteaux", "at_sct_proteaux", "ad_entree_rcb", "en_reserve_faune", "en_courbes_niveau", "en_imns_l", "ep_vanne_batiment", "ep_vanne_parcelle", "ep_conduite_zone_pression", "ep_vanne_raccords", "pc_surf", "pc_parc", "pc_dp_comm", "pc_ddp", "at_lisforet", "at_limconstrforet", "gz_conduite", "gz_vanne", "gz_elemontage", "tc_conduite", "tc_elemontage", "cd_ouvrage", "cd_bat_racc", "cd_cond"),
        "36" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_baths", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "at_camac_anciennes", "at_camac_en_cours", "ep_regulation_pression", "at_lim_constr", "at_class_route", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "en_cit", "en_cit_gest", "en_reserve_faune", "en_imns_l", "en_imns_s", "mo_label_automatique", "ad_entree_rcb", "mo_label", "at_lisforet", "at_limconstrforet", "en_courbes_niveau"),
        "37" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_baths", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "at_surf_assolement", "dn_glissement_prof", "dn_dissolution_gypse", "dn_glissement_surf", "at_lim_constr", "ep_regulation_pression", "en_cit", "en_cit_gest", "en_reserve_faune", "en_imns_s", "en_imns_l", "mo_label_automatique", "ad_entree_rcb", "mo_label", "en_courbes_niveau", "tp_bateau_s", "tp_bus_s", "tp_bus_l", "tp_train_s", "tp_train_l", "pc_surf", "pc_parc", "pc_dp_comm", "pc_ddp"),
        "38" -> Array("at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_baths", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_surf_assolement", "at_pga_ppa", "at_pga", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "tp_train_s", "tp_bus_s", "tp_bateau_s", "tp_bus_l", "tp_train_l", "at_camac_anciennes", "at_camac_en_cours", "ep_regulation_pression", "at_lim_constr", "mo_label", "mo_cs", "tx_zone_public", "tx_zone_prive", "en_cit", "en_cit_gest", "en_reserve_faune", "en_imns_l", "en_imns_s", "mo_label_automatique", "ad_entree_rcb", "en_courbes_niveau", "at_lisforet", "at_limconstrforet", "coll_precis_pl", "eu_coll_sans_precis_pl"),
        "39" -> Array("at_pga", "at_dsb", "at_sct_proteaux", "at_zone_proteaux", "mo_batss", "mo_baths", "mo_ddp", "cad_batss", "cad_odl", "cad_baths", "cad_adr", "ep_hydrante", "ep_captage", "ep_reservoir", "ep_station_pompage", "ep_station_traitement", "ep_vanne", "ad_va_rue_tr", "mo_par", "eu_ch", "eu_coll", "ep_conduite", "mo_agr", "ep_cond", "eu_ch_label", "at_ra_pt", "at_ra_surf", "el_luminaire", "el_armoire", "el_cable", "el_tube", "eu_evac_par", "at_pga_ppa", "dn_glissement_prof", "dn_glissement_surf", "dn_dissolution_gypse", "at_camac_anciennes", "at_camac_en_cours", "at_lim_constr", "ep_regulation_pression", "mo_label", "mo_cs", "tp_bateau_s", "tp_bus_s", "tp_train_s", "tp_train_l", "tp_bus_l", "mo_label_automatique", "en_cit", "en_cit_gest", "en_reserve_faune", "en_imns_l", "en_imns_s", "ad_entree_rcb", "at_lisforet", "at_limconstrforet", "en_courbes_niveau")
    )


    val common_bbox = getBbox(2060, 1215)_
    val map_feeder = new Feeder[String] {
        // always return true as this feeder can be polled infinitively
        override def hasNext = true
        override def next: Map[String, String] = {
            val instance_id = instance_ids.get(random.nextInt(instance_ids.length))
            val map = Map(
                "instance_id" -> instance_id,
//                "suffix" -> "_" + instance_id,
                "suffix" -> "",
                "x" -> "%d".format(randX),
                "y" -> "%d".format(randY),
                "layer" -> layers.get(instance_id).get(random.nextInt(layers.get(instance_id).get.length)),
                "getlegend" -> Map(
                    "cache_version" -> cache_version,
                    "SERVICE" -> "WMS",
                    "VERSION" -> "1.1.1",
                    "REQUEST" -> "GetLegendGraphic",
                    "FORMAT" -> "image%2Fpng",
                    "TRANSPARENT" -> "TRUE",
                    "EXCEPTIONS" -> "=application%2Fvnd.ogc.se_xml",
                    "LAYER" -> layers.get(instance_id).get(random.nextInt(layers.get(instance_id).get.length)),
                    "SCALE" -> 72.0 / resolutions.get(random.nextInt(resolutions.length)) * 39.37
                ).map({ case(k, v) => "%s=%s".format(k, v.toString) }).mkString("&"),
                "coordinates" -> "[[%d,%d],[%d,%d]]".format(
                    randX, randY, randX, randY
                ),
                "fts_qs" -> Map(
                    "limit" -> 20,
                    "query" -> random.nextString(random.nextInt(5) + 1).foldLeft("") { (s, i) => s + fts_choises.charAt(random.nextInt(fts_choises.length))},
                    "_dc" -> cache_version,
                    "callback" -> "stcCallback1001"
                ).map({ case(k, v) => "%s=%s".format(k, v.toString) }).mkString("&")
            )

            map ++ resolutions.map(r => (
                "getmap_%.2f".format(r).replace(".", "_"),
                (Map(
                    "cache_version" -> cache_version,
                    "SERVICE" -> "WMS",
                    "VERSION" -> "1.1.1",
                    "REQUEST" -> "GetMap",
                    "FORMAT" -> "image%2Fpng",
                    "TRANSPARENT" -> "TRUE",
                    "LAYERS" -> layers.get(instance_id).get(random.nextInt(layers.get(instance_id).get.length)),
                    "STYLES" -> "",
                    "SRS" -> "EPSG%3A21781"
                ) ++ common_bbox(randX, randY, r))
                .map({ case(k, v) => "%s=%s".format(k, v.toString) }).mkString("&")
            )).toMap
        }
    }

    val httpProtocol = http
        .baseURL("http://" + host)
        .inferHtmlResources(WhiteList("http://" + host + "/*"), BlackList())
        .acceptHeader("image/png,image/*;q=0.8,*/*;q=0.5")
        .acceptEncodingHeader("gzip, deflate")
        .acceptLanguageHeader(lang + ";q=0.5")
        .connection("keep-alive")
        .contentTypeHeader("application/x-www-form-urlencoded; charset=UTF-8")
        .userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:38.0) Gecko/20100101 Firefox/38.0")

    val headers_0 = Map(
        "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
    )

    val headers_3 = Map(
        "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "X-Requested-With" -> "XMLHttpRequest"
    )

    val headers_8 = Map(
        "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "Pragma" -> "no-cache",
        "X-Requested-With" -> "XMLHttpRequest"
    )

    val headers_18 = Map("Accept" -> "*/*")

    val uri = "http://" + host + "/"

    val scn1 = scenario("RudazSimulation")
        .feed(defaults_feeder)
        .feed(map_feeder)
        .exec(http("index${suffix}").get("/${instance_id}/").headers(headers_0))
        .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getmap_20_00}"))
        .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getmap_20_00}"))
        .pause(8)
        .feed(map_feeder)
        .exec(http("static${suffix}").get("/${instance_id}/wsgi/proj/lib/cgxp/geoext/resources/images/gray/anchor.png"))
        .exec(http("static${suffix}").get("/${instance_id}/wsgi/proj/lib/cgxp/geoext/resources/images/gray/anchor-top.png"))
        .pause(1)
    val scn2 = (0 until nbTimes).foldLeft(scn1) { (s, i) =>
        s.feed(map_feeder)
            .feed(map_feeder)
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getmap_20_00}"))
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getmap_10_00}"))
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getmap_5_00}"))
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getmap_2_00}"))
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getmap_1_00}"))
            .pause(1)
            .feed(map_feeder)
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getmap_0_50}"))
            .pause(2)
            .feed(map_feeder)
            .exec(http("static${suffix}").get("/${instance_id}/wsgi/proj/lib/cgxp/ext/Ext/resources/images/default/grid/loading.gif"))
            .exec(http("fulltextsearch${suffix}").get("/${instance_id}/wsgi/fulltextsearch?${fts_qs}").headers(headers_18))
            .pause(1)
            .feed(map_feeder)
            .exec(http("fulltextsearch${suffix}").get("/${instance_id}/wsgi/fulltextsearch?limit=20&${fts_qs}").headers(headers_18))
            .pause(1)
            .feed(map_feeder)
            .exec(http("fulltextsearch${suffix}").get("/${instance_id}/wsgi/fulltextsearch?limit=20&${fts_qs}").headers(headers_18))
            .pause(1)
            .feed(map_feeder)
            .exec(http("fulltextsearch${suffix}").get("/${instance_id}/wsgi/fulltextsearch?limit=20&${fts_qs}").headers(headers_18))
            .pause(1)
            .feed(map_feeder)
            .exec(http("fulltextsearch${suffix}").get("/${instance_id}/wsgi/fulltextsearch?limit=20&${fts_qs}").headers(headers_18))
            .pause(2)
            .feed(map_feeder)
            .exec(http("static${suffix}").get("/${instance_id}/wsgi/proj/lib/cgxp/ext/Ext/resources/images/gray/form/clear-trigger.gif"))
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getmap_200_00}"))
            .pause(9)
            .feed(map_feeder)
            .exec(http("static${suffix}").get("/${instance_id}/wsgi/proj/lib/cgxp/core/src/theme/img/legend.png"))
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getlegend}"))
            .exec(http("static${suffix}").get("/${instance_id}/wsgi/proj/lib/cgxp/core/src/theme/img/legend-up.png"))
            .feed(map_feeder)
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getlegend}"))
            .feed(map_feeder)
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getlegend}"))
            .feed(map_feeder)
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getlegend}"))
            .pause(2)
            .feed(map_feeder)
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getmap_100_00}"))
            .pause(1)
            .feed(map_feeder)
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getmap_50_00}"))
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getmap_20_00}"))
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getmap_10_00}"))
            .pause(1)
            .feed(map_feeder)
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getlegend}"))
            .feed(map_feeder)
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getlegend}"))
            .feed(map_feeder)
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getlegend}"))
            .feed(map_feeder)
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getlegend}"))
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getmap_5_00}"))
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getmap_2_00}"))
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getmap_1_00}"))
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getmap_0_50}"))
            .pause(2)
            .feed(map_feeder)
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getmap_0_25}"))
            .pause(1)
            .feed(map_feeder)
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getlegend}"))
            .feed(map_feeder)
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getlegend}"))
            .feed(map_feeder)
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getlegend}"))
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getmap_0_10}"))
            .feed(map_feeder)
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?${getlegend}"))
            .pause(2)
            .exec(http("short${suffix}").post(uri + "${instance_id}/wsgi/short/create").headers(headers_8)
                .formParam("url", uri + "${instance_id}/theme/${theme}?map_x=${x}&map_y=${y}&map_zoom=10"))
            .pause(9)
    }
    val scn3 = scn2.exec(http("login${suffix}")
            .post(uri + "${instance_id}/wsgi/login")
            .headers(headers_8)
            .formParam("login", "admin")
            .formParam("password", "c2c")
            .formParam("newPassword", "")
            .formParam("confirmNewPassword", ""))
        .exec(http("index${suffix}")
            .post(uri + "${instance_id}/theme/${theme}?map_x=${x}&map_y=${y}&map_zoom=10")
            .headers(headers_0)
            .formParam("login", "admin")
            .formParam("password", "c2c")
            .formParam("newPassword", "")
            .formParam("confirmNewPassword", ""))
        .exec(http("viewer${suffix}").get("/${instance_id}/wsgi/viewer.js?lang=${lang}&permalink_themes=${theme}&role=6&cache_version=${cache_version}").headers(headers_18))
        .pause(1)
    val scn4 = (0 until nbTimes).foldLeft(scn3) { (s, i) =>
        s.feed(map_feeder)
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?role=6&${getmap_0_10}"))
            .feed(map_feeder)
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?role=6&${getmap_0_10}"))
            .feed(map_feeder)
            .pause(3)
            .feed(map_feeder)
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?role=6&${getmap_2_00}"))
            .pause(5)
            .exec(http("fulltextsearch${suffix}").get("/${instance_id}/wsgi/fulltextsearch?${fts_qs}").headers(headers_18))
            .pause(1)
            .feed(map_feeder)
            .exec(http("fulltextsearch${suffix}").get("/${instance_id}/wsgi/fulltextsearch?${fts_qs}").headers(headers_18))
            .feed(map_feeder)
            .exec(http("fulltextsearch${suffix}").get("/${instance_id}/wsgi/fulltextsearch?${fts_qs}").headers(headers_18))
            .pause(1)
            .feed(map_feeder)
            .exec(http("fulltextsearch${suffix}").get("/${instance_id}/wsgi/fulltextsearch?${fts_qs}").headers(headers_18))
            .pause(1)
            .feed(map_feeder)
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?role=6&${getmap_10_00}"))
            .pause(1)
            .feed(map_feeder)
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?role=6&${getmap_5_00}"))
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?role=6&${getmap_2_00}"))
            .pause(4)
            .feed(map_feeder)
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?role=6&${getlegend}"))
            .feed(map_feeder)
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?role=6&${getlegend}"))
            .pause(1)
            .feed(map_feeder)
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?role=6&${getlegend}"))
            .pause(1)
            .feed(map_feeder)
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?role=6&${getlegend}"))
            .pause(3)
            .feed(map_feeder)
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?role=6&${getmap_2_00}"))
            .pause(1)
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?role=6&${getlegend}"))
            .feed(map_feeder)
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?role=6&${getlegend}"))
            .feed(map_feeder)
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?role=6&${getlegend}"))
            .feed(map_feeder)
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?role=6&${getlegend}"))
            .pause(6)
            .feed(map_feeder)
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?role=6&${getmap_5_00}"))
            .feed(map_feeder)
            .exec(http("mapserv_GetMap${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?role=6&${getmap_5_00}"))
            .pause(1)
            .feed(map_feeder)
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?role=6&${getlegend}"))
            .feed(map_feeder)
            .exec(http("mapserv_GetLegendGraphic${suffix}").get("/${instance_id}/wsgi/mapserv_proxy?role=6&${getlegend}"))
            .pause(10)
            .exec(http("short${suffix}").post(uri + "${instance_id}/wsgi/short/create").headers(headers_8)
                .formParam("url", uri + "${instance_id}/theme/${theme}?map_x=${x}&map_y=${y}&map_zoom=5&tree_group_layers_${group}=${layer}"))
            .pause(1)
    }

    setUp(scn4.inject(rampUsers(nbUser) over (rampTime seconds))).protocols(httpProtocol)
}
