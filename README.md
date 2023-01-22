## Файлы конфигурации клиента:

- `apploader.properties`
- `apploader_<profile>.properties`: имеет приоритет, если задан профиль.

### Свойства конфигурации клиента:
- `server.url`
- `ignore.warnings`

## Файлы конфигурации сервера:

- `install.properties`: наследуется всеми профилями.
- `install_<profile>.properties`: имеет приоритет, если задан профиль.

### Свойства конфигурации сервера:
- `inno.dir`: папка, в которой установлен Inno Setup. По умолчанию `%ProgramFiles%\Inno Setup 5`.
- `rar.dir`: папка, в которой установлен RAR. По умолчанию `%ProgramFiles%\WinRAR` для Windows и `/bin`, `/usr/bin` или `/usr/local/bin` для Linux.
- `makeself`: путь к исполняемому файлу makeself. По умолчанию `makeself` или `makeself.sh` в папках `/bin`, `/usr/bin` или `/usr/local/bin`.

- `server.url`: если файлы `apploader.properties` и `apploader_<profile>.properties` отсутствуют, то сгенерировать файл с этим значением `server.url`. По умолчанию берется из адреса в строке браузера.
- `java.dir`: директория, откуда брать Java для дистрибутива клиента. По умолчанию значение свойства `java.home`.
- `java.zip`: zip-архив, откуда брать Java для дистрибутива клиента.
- `base.name`: базовое имя файла дистрибутива клиента. Расширение будет в зависимости от используемой программы создания дистрибутива. По умолчанию `install`.
- `packers`: разделенный запятыми список программ создания дистрибутива (`inno`, `rar`, `makeself`, `7zsfx`, `7z`, `zip`). По умолчанию список определяется в зависимости от ОС клиента и сервера.

### Доп. требования для программ создания дистрибутива:
- `inno`: должны присутствовать файлы `client.iss` (имя файла можно изменить заданием свойства сервера `inno.script`) и `common.iss`.
- `rar`: должен присутствовать файл `sfx.cfg` (имя файла можно изменить заданием свойства сервера `rar.cfg`).
- `makeself`: должен присутствовать файл `makeself-setup.sh` (имя файла можно изменить заданием свойства сервера `makeself.script`) либо указано свойство сервера `makeself.dir` (в этом случае `makeself` будет просто распаковывать архив в эту папку).
- `7zsfx`: должен присутствовать файл модуля SFX (имя файла можно изменить заданием свойства сервера `7z.sfx.win`/`7z.sfx.lin`, конфигурация модуля задается свойством `7z.sfx.cfg.win`/`7z.sfx.cfg.lin`).
