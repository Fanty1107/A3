import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.awt.Desktop;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    static String userHome = System.getProperty("user.home");
    static File logFile = new File("log.txt");
    static String[] pastasPadrao = {"Documents", "Pictures", "Videos", "Downloads"};

    public static void main(String[] args) {
        System.out.println("Usuário logado: " + System.getProperty("user.name"));
        while (true) {
            mostrarMenu();
            int opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1: abrirArquivoOuPasta(); break;
                case 2: renomear(); break;
                case 3: mover(); break;
                case 4: copiarColar(); break;
                case 5: excluir(); break;
                case 6: System.out.println("Encerrando o sistema..."); return;
                default: System.out.println("Opção inválida!");
            }
        }
    }

    static void mostrarMenu() {
        System.out.println("\n--- MENU ---");
        System.out.println("1. Abrir arquivos ou pastas");
        System.out.println("2. Renomear arquivos ou pastas");
        System.out.println("3. Mover arquivos ou pastas");
        System.out.println("4. Copiar e Colar arquivos");
        System.out.println("5. Excluir arquivos ou pastas");
        System.out.println("6. Sair");
        System.out.print("Escolha uma opção: ");
    }

    static File selecionarArquivo() {
        File[] unidades = File.listRoots();

        System.out.println("\nPastas padrão:");
        for (int i = 0; i < pastasPadrao.length; i++) {
            System.out.println((i + 1) + ". " + pastasPadrao[i]);
        }

        System.out.println("\nUnidades disponíveis:");
        for (int i = 0; i < unidades.length; i++) {
            System.out.println((i + 1 + pastasPadrao.length) + ". " + unidades[i]);
        }

        System.out.print("Escolha o número (ou 0 para sair): ");
        int escolha = scanner.nextInt();
        scanner.nextLine();

        if (escolha == 0) {
            System.out.println("Encerrando o sistema...");
            System.exit(0);
        }

        File diretorio;
        if (escolha <= pastasPadrao.length) {
            diretorio = new File(userHome + File.separator + pastasPadrao[escolha - 1]);
        } else {
            diretorio = unidades[escolha - pastasPadrao.length - 1];
        }

        File[] arquivos = diretorio.listFiles();
        if (arquivos == null || arquivos.length == 0) {
            System.out.println("Nenhum conteúdo.");
            return null;
        }

        for (int i = 0; i < arquivos.length; i++) {
            System.out.println((i + 1) + ". " + (arquivos[i].isDirectory() ? "[DIR] " : "[ARQ] ") + arquivos[i].getName());
        }

        System.out.print("Escolha um arquivo ou pasta ou 0 para sair: ");
        int index = scanner.nextInt();
        scanner.nextLine();

        if (index == 0) {
            System.out.println("Encerrando o sistema...");
            System.exit(0);
        }

        if (index > 0 && index <= arquivos.length) {
            return arquivos[index - 1];
        }

        return null;
    }

    static void abrirArquivoOuPasta() {
        File selecionado = selecionarArquivo();
        if (selecionado != null) {
            try {
                Desktop.getDesktop().open(selecionado);
                registrarLog("Aberto: " + selecionado.getAbsolutePath());
            } catch (Exception e) {
                System.out.println("Erro ao abrir: " + e.getMessage());
            }
        }
    }

    static void renomear() {
        File selecionado = selecionarArquivo();
        if (selecionado == null) return;

        String extensao = "";
        String nomeOriginal = selecionado.getName();
        int ponto = nomeOriginal.lastIndexOf('.');

        if (selecionado.isFile() && ponto != -1) {
            extensao = nomeOriginal.substring(ponto);
        }

        System.out.println("Nome atual: " + nomeOriginal);
        System.out.print("Novo nome (sem extensão): ");
        String novoNome = scanner.nextLine();

        File novoArquivo = new File(selecionado.getParent(), novoNome + extensao);

        if (selecionado.renameTo(novoArquivo)) {
            System.out.println("Renomeado com sucesso para: " + novoArquivo.getName());
            registrarLog("Renomeado: " + selecionado.getAbsolutePath() + " para " + novoArquivo.getAbsolutePath());
        } else {
            System.out.println("Falha ao renomear.");
        }
    }

    static void mover() {
        File origem = selecionarArquivo();
        if (origem == null) return;

        File destinoDir = selecionarArquivo();
        if (destinoDir == null || !destinoDir.isDirectory()) {
            System.out.println("Destino inválido.");
            return;
        }

        try {
            Path result = Files.move(origem.toPath(), Paths.get(destinoDir.getAbsolutePath(), origem.getName()), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Movido para: " + result);
            registrarLog("Movido: " + origem + " para " + result);
        } catch (IOException e) {
            System.out.println("Erro ao mover: " + e.getMessage());
        }
    }

    static void copiarColar() {
        File origem = selecionarArquivo();
        if (origem == null) return;

        System.out.println("Digite o caminho completo do diretório de destino Ex(C:\\Users\\Nome do usuário\\Downloads): ");
        String caminhoDestino = scanner.nextLine();
        File destinoDir = new File(caminhoDestino);

        if (!destinoDir.exists() || !destinoDir.isDirectory()) {
            System.out.println("Destino inválido. Verifique se o caminho está correto e é um diretório.");
            return;
        }

        File destino = new File(destinoDir, origem.getName());

        try {
            Files.copy(origem.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Arquivo copiado para: " + destino.getAbsolutePath());
            registrarLog("Copiado: " + origem.getAbsolutePath() + " para " + destino.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Erro ao copiar: " + e.getMessage());
        }
    }

    static void excluir() {
        File selecionado = selecionarArquivo();
        if (selecionado == null) return;
        System.out.print("Tem certeza que deseja excluir? (s/n): ");
        String confirmacao = scanner.nextLine();
        if (!confirmacao.equalsIgnoreCase("s")) {
            System.out.println("Exclusão cancelada.");
            return;
        }
        boolean sucesso = true;
        if (selecionado.isDirectory()) {
            File[] arquivos = selecionado.listFiles();
            if (arquivos != null) {
                for (File f : arquivos) {
                    sucesso = excluirArquivoOuPasta(f);
                    if (!sucesso) break;
                }
            }
        }
        if (sucesso) {
            sucesso = selecionado.delete();
        }

        if (sucesso) {
            System.out.println("Excluído com sucesso.");
            registrarLog("Excluído: " + selecionado.getAbsolutePath());
        } else {
            System.out.println("Falha ao excluir.");
        }
    }

    private static boolean excluirArquivoOuPasta(File arquivo) {
        if (arquivo.isDirectory()) {
            File[] arquivos = arquivo.listFiles();
            if (arquivos != null) {
                for (File f : arquivos) {
                    if (!excluirArquivoOuPasta(f)) return false;
                }
            }
        }
        return arquivo.delete();
    }
    static void registrarLog(String acao) {
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(new Date() + " - " + acao + "\n");
        } catch (IOException e) {
            System.out.println("Erro ao registrar log: " + e.getMessage());
        }
    }
}