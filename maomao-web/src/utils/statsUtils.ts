export async function getAverageColor(url: string): Promise<string> {
    return new Promise((resolve) => {
        const img = new Image();
        img.crossOrigin = 'Anonymous';
        img.src = url;

        img.onload = function () {
            const canvas = document.createElement('canvas');
            canvas.width = img.width;
            canvas.height = img.height;

            const ctx = canvas.getContext('2d');
            if (!ctx) return resolve('rgba(100, 100, 100, 0.6)');

            ctx.drawImage(img, 0, 0);

            const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
            const { data } = imageData;

            let r = 0, g = 0, b = 0, count = 0;

            for (let i = 0; i < data.length; i += 4) {
                r += data[i];
                g += data[i + 1];
                b += data[i + 2];
                count++;
            }

            r = Math.floor(r / count);
            g = Math.floor(g / count);
            b = Math.floor(b / count);

            resolve(`rgba(${r}, ${g}, ${b}, 0.6)`);
        };

        img.onerror = function () {
            resolve('rgba(100, 100, 100, 0.6)');
        };
    });
}

export function formatDuration(ms: number): string {
    const units = [
        { label: 'y', ms: 365 * 24 * 60 * 60 * 1000 },
        { label: 'mo', ms: 30 * 24 * 60 * 60 * 1000 },
        { label: 'd', ms: 24 * 60 * 60 * 1000 },
        { label: 'h', ms: 60 * 60 * 1000 },
        { label: 'min', ms: 60 * 1000 },
        { label: 's', ms: 1000 }
    ];

    const result: string[] = [];

    for (const unit of units) {
        const value = Math.floor(ms / unit.ms);
        if (value > 0) {
            result.push(`${value}${unit.label}`);
            ms -= value * unit.ms;
        }
        if (result.length === 2) break;
    }

    return result.length ? result.join(' ') : '0s';
}