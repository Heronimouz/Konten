import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static List<Konto> konten = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        List<BuchungsCache> caches = new ArrayList<>();

        Konto ebk = null;
        Konto sbk = null;

        BufferedReader csvReader = new BufferedReader(new FileReader("Konten.csv"));
        String row = null;
        boolean isNewKonto = true;
        Konto newKontoCreate = null;
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(",");
            if (data.length <= 1) {
                isNewKonto = true;
                continue;
            }

            if (isNewKonto) {
                newKontoCreate = new Konto(data[2], data.length > 6 ? getKontoForName(data[6]) : null, data[5].equalsIgnoreCase("B") ? KontoTyp.BILANZKONTO : data[5].equalsIgnoreCase("A") ? KontoTyp.AKTIV : KontoTyp.PASSIV);
                konten.add(newKontoCreate);
                if (newKontoCreate.getName().equalsIgnoreCase("ebk")) {
                    ebk = newKontoCreate;
                }
                else if (newKontoCreate.getName().equalsIgnoreCase("sbk")) {
                    sbk = newKontoCreate;
                }
            }
            else {
                if (!data[0].equalsIgnoreCase("")) {
                    caches.add(new BuchungsCache(newKontoCreate.getName(), data[0], new BigDecimal(data[1])));
                }
                if (data.length > 2 && !data[3].equalsIgnoreCase("")) {
                    caches.add(new BuchungsCache(data[3], newKontoCreate.getName(), new BigDecimal(data[4])));
                }
            }


            isNewKonto = false;
        }

        if (ebk == null || sbk == null) {
            throw new IllegalStateException("EBK and SBK Konto are necessary");
        }

        List<BuchungsCache> existingBuchungen = new ArrayList<>();
        List<BuchungsCache> buchungDeleteList = new ArrayList<>();
        for (BuchungsCache buchung : caches) {
            List<BuchungsCache> buchungsCaches = existingBuchungen.stream()
                    .filter(existingBuchung -> existingBuchung.betrag.compareTo(buchung.betrag) == 0
                            && existingBuchung.soll.equalsIgnoreCase(buchung.soll)
                            && existingBuchung.haben.equalsIgnoreCase(buchung.haben)).collect(Collectors.toList());
            if (!buchungsCaches.isEmpty()) {
                existingBuchungen.removeAll(buchungsCaches);
                buchungDeleteList.add(buchung);
            }
            else {
                existingBuchungen.add(buchung);
            }
        }
        caches.removeAll(buchungDeleteList);

        for (BuchungsCache buchung : caches) {
            Konto sollKonto = getKontoForName(buchung.soll);
            Konto habenKonto = getKontoForName(buchung.haben);

            if (sollKonto == null || habenKonto == null) {
                throw new IllegalStateException("Buchung für nicht existierendes Konto");
            }

            new Buchung(sollKonto, habenKonto, buchung.betrag);
        }

        for (Konto konto: konten) {
            Konto finalSbk = sbk;
            if (konto.getBuchungen().stream().anyMatch(buchung -> buchung.getHabenKonto().equals(finalSbk)||buchung.getSollKonto().equals(finalSbk))) {
                konto.setAbgeschlossen();
            }
            System.out.println(konto.print());
        }

        String command = IOTools.readLine();
        while (!command.equalsIgnoreCase("exit")) {
            try {
                String[] commandWords = command.split(" ");
                switch (commandWords[0]) {
                    case "create":

                        Konto parentKonto = ebk;
                        if (commandWords.length == 4) {
                            Konto foundKonto = getKontoForName(commandWords[3]);
                            parentKonto = foundKonto == null ? parentKonto : foundKonto;
                        }

                        KontoTyp typ = KontoTyp.PASSIV;
                        if (commandWords.length == 3 && commandWords[2].equalsIgnoreCase("A")) {
                            typ = KontoTyp.AKTIV;
                        }

                        Konto newKonto = new Konto(commandWords[1], parentKonto, typ);
                        konten.add(newKonto);
                        System.out.println(newKonto.print());
                        break;
                    case "b":
                        Konto sollKonto = getKontoForName(commandWords[1]);
                        Konto habenKonto = getKontoForName(commandWords[2]);
                        if (sollKonto == null) {
                            System.out.println("Konto nicht gefunden: " + commandWords[1]);
                        }
                        if (habenKonto == null) {
                            System.out.println("Konto nicht gefunden: " + commandWords[2]);
                        }
                        if (sollKonto == null || habenKonto == null) {
                            break;
                        }

                        BigDecimal betrag = new BigDecimal(commandWords[3]);
                        Buchung buchung = new Buchung(sollKonto, habenKonto, betrag);
                        System.out.println(buchung.erstelleBuchungssatz());
                        System.out.println(sollKonto.print());
                        System.out.println(habenKonto.print());
                        break;
                    case "saldo":
                        for (Konto konto : ebk.getChildKontos()) {
                            konto.saldieren(sbk);
                        }

                        if (sbk.getSollGesamt().compareTo(sbk.getHabenGesamt()) != 0) {
                            System.out.println(sbk.print());
                            throw new IllegalStateException("Soll und Haben sind unterschiedlich!");
                        }
                    case "print":
                        for (Konto konto : konten) {
                            System.out.println(konto.print());
                        }
                        break;
                    case "del":
                        Konto foundKonto = getKontoForName(commandWords[1]);
                        if (foundKonto != null) {
                            konten.remove(foundKonto);
                            foundKonto.getParentKonto().getChildKontos().remove(foundKonto);
                            foundKonto.getChildKontos().forEach(konto -> konto.setParentKonto(null));
                            foundKonto.getBuchungen().forEach(buchung1 -> {
                                buchung1.getHabenKonto().getHabenBuchungen().remove(buchung1);
                                buchung1.getSollKonto().getSollBuchungen().remove(buchung1);
                            });
                            System.out.println("delete of " + commandWords[1] + " successfull");
                        }
                        break;
                    default:
                        System.out.println("unknown command!");
                        break;
                }
            }
            catch (Exception ex) {
                if (ex instanceof IllegalStateException) {
                    throw new IllegalStateException(ex);
                }
                System.out.println("false parameter!");
                System.out.println(ex.getMessage());
            }
            command = IOTools.readLine();
        }

        FileWriter csvWriter = new FileWriter("Konten.csv");

        for (Konto konto : konten) {
            csvWriter.append("S,,");
            csvWriter.append(konto.getName());
            csvWriter.append(",,H,");
            csvWriter.append(konto.getTyp().name().substring(0,1));
            if (konto.getParentKonto() != null) {
                csvWriter.append(",");
                csvWriter.append(konto.getParentKonto().getName());
            }
            csvWriter.append("\n");
            for (int i = 0; i < (Math.max(konto.getSollBuchungen().size(), konto.getHabenBuchungen().size())); i++) {
                if (konto.getSollBuchungen().size() > i) {
                    Buchung buchung = konto.getSollBuchungen().get(i);
                    csvWriter.append(buchung.getHabenKonto().getName());
                    csvWriter.append(",");
                    csvWriter.append(buchung.getBetrag().toString());
                    csvWriter.append(",");
                }
                else {
                    csvWriter.append(",,");
                }
                csvWriter.append(",");
                if (konto.getHabenBuchungen().size() > i) {
                    Buchung buchung = konto.getHabenBuchungen().get(i);
                    csvWriter.append(buchung.getSollKonto().getName());
                    csvWriter.append(",");
                    csvWriter.append(buchung.getBetrag().toString());
                }
                else {
                    csvWriter.append(",");
                }
                csvWriter.append("\n");
            }
            csvWriter.append("\n");
        }

        csvWriter.flush();
        csvWriter.close();
    }

    private static Konto getKontoForName(String name) {
        List<Konto> kontos = konten.stream().filter(konto -> konto.getName().equalsIgnoreCase(name)).collect(Collectors.toList());
        return kontos.isEmpty() ? null : kontos.get(0);
    }

    public static class BuchungsCache {
        String soll;
        String haben;
        BigDecimal betrag;

        public BuchungsCache(String soll, String haben, BigDecimal betrag) {
            this.soll = soll;
            this.haben = haben;
            this.betrag = betrag;
        }
    }

}
