[![CI](https://github.com/Ch-LZ/Scinetific-iterator/actions/workflows/gradle.yml/badge.svg?branch=main)](https://github.com/Ch-LZ/Scinetific-iterator/actions/workflows/gradle.yml)
# Scinetific-iterator


# description

Wait-free snapshot-based iterator for concurrent list. Implemented for lock-free list according to the article.
A lot of comments around the code contain (or consist of) pieces of the article.

Все комментарии в коде посвящены wait-free итератору.
Авторы статьи приводят рецепт wait-free реализации, который удалось воплотить в жизнь
<p>Основная идея итератора: когда некоторый поток начинает снимать shapshot, потоки,
изменяющие структуру данных должны предоставить report'ы об изменениях. Для поддержания порядка операций потоки-писатели должны отсылать отчёты и о "чужих уcпехах", в случаях если
1) либо требуется чтобы "чужие успехи" линеаризовались раньше соответствующих операций потоков-писателей,
2) или есть опасность, что итератор не отразит изменений других потоков. Это позволяет сделать снапшот согласованным.
<p>После однократного сканирования списка целиком, параллельно собранные report'ы используются для валидизирования снапшота.
<p>
Главный объект во всём алгоритме снятия снапшота - SnapCollector.
Он отвечает за "коммуникацию" между потоками и одновременную линеаризацию отчетов от всех (весь секрет в CAS - операции).
Хранение report'ов и отсканированных узлов списка он делегирует двум хранилищам - ReportStorage и ScannerStorage соответственно.
Хранилища имеют общую внутреннюю логику основанную на известной lock-free очереди Майкла Скотта, реализованную в GenericStorage.
Сами хранилища отличаются только типом хранимых элементов.
Дополнительный объект Snapshotmanager отвечает за пост-обработку снапшота и призван насколько возможно логически обособить логику снятия снапшота от логики работы множества. 
А также логически пометить дополнительные операции в алгоритме lock-free множества.
<p>

# implementation notes

В хранилищах собираются не значения, а ссылки на узлы. Это необходимо, чтобы использовать их как уникальные
идентификаторы,
чтобы различать разные элементы с одинаковыми значениями на этапе пост-обработки снапшота.

Оптимизации (по признанию авторов небольшие) из параграфа 5.2 статьи в реализации отсутствуют.

# sources

- lock-free set algorythm is implemented according to the book \
  Herlihy M., Shavit N. The Art of Multiprocessor Programming (220 - 226)
- iterator algorithm is implemented almost according to the article but with some restrictions. \
  Petrank E., Timnat S. Lock-free data-structure iterators //International Symposium on Distributed Computing. –
  Springer, Berlin, Heidelberg, 2013. – С. 224-238.
  https://link.springer.com/chapter/10.1007/978-3-642-41527-2_16
