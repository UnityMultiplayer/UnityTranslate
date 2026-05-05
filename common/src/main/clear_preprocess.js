const fs = require('fs');
const path = require('path');

const files = fs.readdirSync('.', { recursive: true });

for (const file of files) {
    if (!file.endsWith('.java') && !file.endsWith('.kt'))
        continue;

    if (!file.includes('essential') || !file.includes('universal'))
        continue;

    const data = fs.readFileSync(file).toString();
    let lines = data.split('\n');

    for (let i = 0; i < lines.length; i++) {
        const line = lines[i].trim();
        if (line.startsWith('//#') || line.startsWith('//$$')) {
            lines.splice(i, 1);
            i--;
        }
    }

    fs.writeFileSync(file, lines.join('\n'));
}
