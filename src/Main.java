import java.io.*; //importa classes para manipulação de arquivos
import java.nio.file.*; //importa classes para copiar/mover arquivos
import java.util.*; //importa classes utilitárias como Scanner e Date
import java.awt.Desktop; //permite abrir arquivos com o programa padrão do sistema

public class Main {
    static Scanner scanner = new Scanner(System.in); //cria scanner para leitura de entradas
    static String userHome = System.getProperty("user.home"); //pega o diretório do usuário logado
    static File logFile = new File("log.txt"); //cria um arquivo de log
    static String[] pastasPadrao = {"Documents", "Pictures", "Videos", "Downloads"}; //define as pastas padrão

    public static void main(String[] args) {
        System.out.println("Usuário logado: " + System.getProperty("user.name")); //exibe o nome do usuário logado
        while (true) { //laço principal do sistema
            mostrarMenu(); //exibe o menu
            int opcao = scanner.nextInt(); //lê a opção escolhida
            scanner.nextLine(); //limpa o buffer

            switch (opcao) {
                case 1: abrirArquivoOuPasta(); break; //opção para abrir arquivo ou pasta
                case 2: renomear(); break; //opção para renomear
                case 3: mover(); break; //opção para mover
                case 4: copiar(); break; //opção para copiar
                case 5: excluir(); break; //opção para excluir
                case 6: System.out.println("Encerrando o sistema..."); return; //encerra o programa
                default: System.out.println("Opção inválida!"); //entrada inválida
            }
        }
    }

    static void mostrarMenu() {
        System.out.println("\n--- MENU ---");
        System.out.println("1. Abrir arquivos ou pastas");
        System.out.println("2. Renomear arquivos ou pastas");
        System.out.println("3. Mover arquivos");
        System.out.println("4. Copiar arquivos");
        System.out.println("5. Excluir arquivos ou pastas");
        System.out.println("6. Sair");
        System.out.print("Escolha uma opção: ");
    }

    static File selecionarArquivo() {
        File[] unidades = File.listRoots(); //pega as unidades do sistema e armazena em um array


        System.out.println("\nPastas padrão:");
        for (int i = 0; i < pastasPadrao.length; i++) {
            System.out.println((i + 1) + ". " + pastasPadrao[i]); //exibe pastas padrão
        }

        System.out.println("\nUnidades disponíveis:"); //exibe unidades disponíveis (C:\, D:\ etc.)
        for (int i = 0; i < unidades.length; i++) {
            System.out.println((i + 1 + pastasPadrao.length) + ". " + unidades[i]);
        }

        System.out.print("Escolha um número (ou 0 para sair): ");
        int escolha = scanner.nextInt(); //lê escolha do usuário
        scanner.nextLine();

        if (escolha == 0) { //se escolher 0, encerra
            System.out.println("Encerrando o sistema...");
            System.exit(0);
        }

        File diretorio; //cria um File representando o diretório escolhido
        if (escolha <= pastasPadrao.length) {
            diretorio = new File(userHome + File.separator + pastasPadrao[escolha - 1]); //monta caminho da pasta padrão
        } else {
            diretorio = unidades[escolha - pastasPadrao.length - 1]; //pega unidade selecionada
        }

        return navegarDiretorio(diretorio); //envia o diretório para navegar e retorna o item final escolhido

    }

    static File navegarDiretorio(File dir) {
        while (true) {
            File[] arquivos = dir.listFiles(); //armazena os arquivos/pastas do diretório selecionado
            if (arquivos == null) { //se não tiver nada, diz que a pasta está vazia
                System.out.println("Pasta vazia.");
                return null;
            }

            System.out.println("\nConteúdo de: " + dir.getAbsolutePath()); //exibe o caminho da pasta atual
            for (int i = 0; i < arquivos.length; i++) {
                System.out.println((i + 1) + ". " + (arquivos[i].isDirectory() ? "[DIR] " : "[ARQ] ") + arquivos[i].getName()); //mostra se é diretório ou arquivo
            }

            System.out.print("Escolha um item, 0 para sair ou -1 para selecionar esta pasta: ");
            int escolha = scanner.nextInt(); //lê a escolha do usuário
            scanner.nextLine();

            if (escolha == 0) {
                System.out.println("Encerrando o sistema...");
                System.exit(0); //encerra o processo
            } else if (escolha == -1) {
                return dir; //retorna a pasta atual
            } else if (escolha > 0 && escolha <= arquivos.length) {
                File escolhido = arquivos[escolha - 1]; //seleciona o item com base no número
                if (escolhido.isDirectory()) {
                    dir = escolhido; //navega para dentro da pasta, volta pro inicio do loop e mostra o conteúdo da pasta
                } else {
                    return escolhido; //retorna o arquivo selecionado
                }
            } else {
                System.out.println("Opção inválida."); //trata número inválido
            }
        }
    }

    static void abrirArquivoOuPasta() {
        File selecionado = selecionarArquivo(); //chama o método para selecionar um arquivo ou pasta
        if (selecionado != null) {
            try {
                Desktop.getDesktop().open(selecionado); //abre o item com o programa padrão do sistema
                registrarLog("Aberto: " + selecionado.getAbsolutePath()); //registra ação no log
            } catch (IOException e) {
                System.out.println("Erro ao abrir: " + e.getMessage()); //trata erro ao abrir
            }
        }
    }

    static void renomear() {
        File selecionado = selecionarArquivo(); //seleciona o item a ser renomeado
        if (selecionado == null) return;

        String extensao = "";
        String nomeOriginal = selecionado.getName(); //pega o nome atual
        int ponto = nomeOriginal.lastIndexOf('.'); //identifica a posição do ponto (extensão)

        if (selecionado.isFile() && ponto != -1) {
            extensao = nomeOriginal.substring(ponto); //separa a extensão se for arquivo
        }

        System.out.println("Nome atual: " + nomeOriginal);
        System.out.print("Novo nome (sem extensão): ");
        String novoNome = scanner.nextLine(); //lê novo nome

        File novoArquivo = new File(selecionado.getParent(), novoNome + extensao); //monta o novo caminho

        if (selecionado.renameTo(novoArquivo)) {
            System.out.println("Renomeado com sucesso para: " + novoArquivo.getName());
            registrarLog("Renomeado: " + selecionado.getAbsolutePath() + " para " + novoArquivo.getAbsolutePath()); //registra no log
        } else {
            System.out.println("Falha ao renomear."); //falha na renomeação
        }
    }

    static void mover() {
        File origem = selecionarArquivo(); //Abre o seletor para o usuário escolher o arquivo a ser movido
        if (origem == null) return; //Se o usuário cancelar, a função termina

        System.out.print("Digite o caminho completo do diretório de destino: (Ex: C:\\Users\\userName\\Downloads)"); //Pede ao usuário que digite o caminho
        String caminho = scanner.nextLine(); //Lê o caminho digitado pelo usuário
        File destinoDir = new File(caminho); //Cria um objeto File apontando para o diretório digitado

        //Verifica se o caminho existe e se é um diretório
        if (!destinoDir.exists() || !destinoDir.isDirectory()) {
            System.out.println("Diretório inválido."); //Se não for válido, exibe mensagem e encerra
            return;
        }

        try {
            //Tenta mover o arquivo para o diretório de destino, substituindo se já existir
            Path result = Files.move(
                    origem.toPath(), //Caminho original
                    Paths.get(destinoDir.getAbsolutePath(), origem.getName()), //Novo caminho
                    StandardCopyOption.REPLACE_EXISTING //Se já existir, substitui
            );
            System.out.println("Movido para: " + result); //Mostra onde foi movido
            registrarLog("Movido: " + origem + " para " + result); //Registra a ação no log
        } catch (IOException e) {
            System.out.println("Erro ao mover: " + e.getMessage()); //Em caso de erro, mostra mensagem
        }
    }
    static void copiar() {
        File origem = selecionarArquivo(); //Abre o seletor para o usuário escolher o arquivo a ser copiado
        if (origem == null) return; //Se o usuário cancelar, a função termina

        System.out.print("Digite o caminho completo do diretório de destino: (Ex: C:\\Users\\userName\\Downloads) "); // Solicita o caminho do destino
        String caminho = scanner.nextLine(); //Lê o caminho digitado
        File destinoDir = new File(caminho); //Cria o objeto File com base no caminho informado

        //Verifica se o caminho existe e se é um diretório
        if (!destinoDir.exists() || !destinoDir.isDirectory()) {
            System.out.println("Diretório inválido."); //Se não for válido, exibe mensagem
            return;
        }

        //Cria o caminho completo do arquivo de destino (mesmo nome que o original)
        File destino = new File(destinoDir, origem.getName());

        try {
            //Tenta copiar o arquivo, substituindo se já existir
            Files.copy(
                    origem.toPath(), //Caminho do arquivo original
                    destino.toPath(), //Caminho do novo arquivo
                    StandardCopyOption.REPLACE_EXISTING //Substitui se já existir
            );
            System.out.println("Cópia criada: " + destino.getAbsolutePath()); //Mostra onde a cópia foi salva
            registrarLog("Cópia criada: " + destino); //Registra a ação no log
        } catch (IOException e) {
            System.out.println("Erro ao copiar: " + e.getMessage()); //Mostra erro, se houver
        }
    }

    static void excluir() {
        File selecionado = selecionarArquivo(); //seleciona o arquivo ou pasta a excluir
        if (selecionado == null) return;

        System.out.print("Tem certeza que deseja excluir? (s/n): ");
        String confirmacao = scanner.nextLine(); //confirmação do usuário
        if (confirmacao.equalsIgnoreCase("n")) {
            System.out.println("Exclusão cancelada."); //cancela a exclusão
            return;
        }

        boolean sucesso = excluirArqDentro(selecionado); //executa a exclusão

        if (sucesso) {
            System.out.println("Excluído com sucesso.");
            registrarLog("Excluído: " + selecionado.getAbsolutePath()); //registra no log
        } else {
            System.out.println("Falha ao excluir."); //erro na exclusão
        }
    }

    static boolean excluirArqDentro(File arquivo) {
        if (arquivo.isDirectory()) { //se for diretório, apaga os arquivos dentro primeiro
            File[] arquivos = arquivo.listFiles();
            if (arquivos != null) {
                for (File f : arquivos) {
                    if (!excluirArqDentro(f)) return false; //exclui todos os arquivos internos
                }
            }
        }
        return arquivo.delete(); //exclui o próprio arquivo ou diretório
    }

    static void registrarLog(String acao) {
        try (FileWriter writer = new FileWriter(logFile, true)) { //abre o arquivo de log para escrita
            writer.write(new Date() + " - " + acao + "\n"); //grava data e ação
        } catch (IOException e) {
            System.out.println("Erro ao registrar log: " + e.getMessage()); //erro ao escrever no log
        }
    }
}

