export class FileUtils {
  public static loadToDataURL(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();

      reader.onload = () => {
        resolve(reader.result?.toString());
      };

      reader.onerror = reject;

      reader.readAsDataURL(file);
    });
  }
}
