import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.Random;

public class Main {
    // Константы для настройки генератора текстов
    private static final String LETTERS = "abc";
    private static final int TEXT_LENGTH = 100000;
    private static final int NUMBER_OF_TEXTS = 10000;

    // Блокирующие очереди для передачи текстов анализирующим потокам
    private static final BlockingQueue<String> queueA = new ArrayBlockingQueue<>(100);
    private static final BlockingQueue<String> queueB = new ArrayBlockingQueue<>(100);
    private static final BlockingQueue<String> queueC = new ArrayBlockingQueue<>(100);

    public static void main(String[] args) {
        // Создание и запуск потоков для анализа символов 'a', 'b' и 'c'
        Thread threadA = new Thread(() -> analyze('a'));
        Thread threadB = new Thread(() -> analyze('b'));
        Thread threadC = new Thread(() -> analyze('c'));
        threadA.start();
        threadB.start();
        threadC.start();

        // Поток для заполнения очередей текстами
        Thread generatorThread = new Thread(() -> {
            for (int i = 0; i < NUMBER_OF_TEXTS; i++) {
                String text = generateText(LETTERS, TEXT_LENGTH);
                try {
                    queueA.put(text);
                    queueB.put(text);
                    queueC.put(text);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // Добавляем специальные строки-сигналы для завершения работы анализирующих потоков
            try {
                queueA.put("");
                queueB.put("");
                queueC.put("");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Запускаем генератор текстов
        generatorThread.start();

        // Ждем завершения анализирующих потоков
        try {
            threadA.join();
            threadB.join();
            threadC.join();
            generatorThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Генератор текстов
    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    // Метод для анализа количества символов в текстах
    private static void analyze(char character) {
        BlockingQueue<String> queue = getQueueForCharacter(character);
        String text;
        int maxCount = 0;
        String maxText = "";

        try {
            while (!(text = queue.take()).isEmpty()) {
                int count = countCharacterOccurrences(text, character);
                if (count > maxCount) {
                    maxCount = count;
                    maxText = text;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Максимальное количество символов '" + character + "' в тексте: " + maxCount);
        System.out.println("Текст с максимальным количеством символов '" + character + "': " + maxText);
    }

    // Вспомогательный метод для получения соответствующей очереди для символа
    private static BlockingQueue<String> getQueueForCharacter(char character) {
        if (character == 'a') {
            return queueA;
        } else if (character == 'b') {
            return queueB;
        } else if (character == 'c') {
            return queueC;
        } else {
            throw new IllegalArgumentException("Неверный символ: " + character);
        }
    }

    // Вспомогательный метод для подсчета количества символов в тексте
    private static int countCharacterOccurrences(String text, char character) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == character) {
                count++;
            }
        }
        return count;
    }
}